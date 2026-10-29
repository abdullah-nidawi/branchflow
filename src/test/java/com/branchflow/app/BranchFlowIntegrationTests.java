package com.branchflow.app;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "branchflow.security.admin.password=admin-test-password-123",
        "branchflow.security.manager.password=manager-test-password-123",
        "branchflow.security.employee.password=employee-test-password-123"
})
@AutoConfigureMockMvc
class BranchFlowIntegrationTests {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void roleRulesProtectWritesAndAllowReads() throws Exception {
        mockMvc.perform(get("/api/companies")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/companies").header("Authorization", basic("employee", "employee-test-password-123")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/companies")
                        .header("Authorization", basic("employee", "employee-test-password-123"))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", "Forbidden Company"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void branchInventoryTasksAndSchedulingPersistThroughPostgres() throws Exception {
        String admin = basic("admin", "admin-test-password-123");
        String manager = basic("manager", "manager-test-password-123");

        JsonNode company = postJson("/api/companies", admin, Map.of("name", "Northstar Integration"));
        long companyId = company.get("id").asLong();
        JsonNode branch = postJson("/api/branches", admin,
                Map.of("companyId", companyId, "name", "Dortmund Center", "city", "Dortmund"));
        long branchId = branch.get("id").asLong();
        JsonNode product = postJson("/api/products", manager,
                Map.of("companyId", companyId, "sku", "cola-1l", "name", "Cola 1 L"));
        long productId = product.get("id").asLong();
        JsonNode stock = postJson("/api/branches/" + branchId + "/stock", manager,
                Map.of("productId", productId, "minimumQuantity", 5));
        long stockId = stock.get("id").asLong();

        postJson("/api/stock/" + stockId + "/movements", manager,
                Map.of("type", "DELIVERY", "quantityChange", 12, "reason", "Supplier delivery"));
        postJson("/api/stock/" + stockId + "/movements", manager,
                Map.of("type", "SALE", "quantityChange", -8, "reason", "Counter sale"));
        mockMvc.perform(get("/api/branches/" + branchId + "/stock/low").header("Authorization", manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].quantity").value(4));
        mockMvc.perform(get("/api/stock/" + stockId + "/movements").header("Authorization", manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));

        JsonNode task = postJson("/api/branches/" + branchId + "/tasks", manager,
                Map.of("title", "Count the front shelf", "priority", "HIGH"));
        mockMvc.perform(get("/api/branches/" + branchId + "/tasks?status=OPEN&page=0&size=10")
                        .header("Authorization", manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(task.get("id").asLong()));

        JsonNode employee = postJson("/api/branches/" + branchId + "/employees", manager,
                Map.of("name", "Alex Example", "email", "alex@example.test"));
        postJson("/api/branches/" + branchId + "/shifts", manager, Map.of(
                "employeeId", employee.get("id").asLong(),
                "startTime", "2026-10-30T08:00:00Z", "endTime", "2026-10-30T16:00:00Z"));
        mockMvc.perform(post("/api/branches/" + branchId + "/shifts")
                        .header("Authorization", manager).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("employeeId", employee.get("id").asLong(),
                                "startTime", "2026-10-30T15:00:00Z", "endTime", "2026-10-30T18:00:00Z"))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("This shift overlaps another active shift for the employee"));
    }

    private JsonNode postJson(String path, String authorization, Map<String, ?> body) throws Exception {
        String response = mockMvc.perform(post(path).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String json(Map<String, ?> value) throws Exception { return objectMapper.writeValueAsString(value); }

    private String basic(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
