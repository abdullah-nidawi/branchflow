import { useEffect, useMemo, useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import './App.css'

type Company = { id: number; name: string }
type Branch = { id: number; companyId: number; name: string; city: string }
type Product = { id: number; companyId: number; sku: string; name: string; active: boolean }
type StockItem = { id: number; branchId: number; productId: number; sku: string; productName: string; quantity: number; minimumQuantity: number; lowStock: boolean }
type Task = { id: number; branchId: number; title: string; description?: string; priority: string; status: string; dueAt?: string; createdAt: string }
type Employee = { id: number; branchId: number; name: string; email: string }
type Shift = { id: number; branchId: number; employeeId: number; employeeName: string; startTime: string; endTime: string; status: string }
type Page<T> = { content: T[]; totalElements: number }
type Notice = { kind: 'success' | 'error'; text: string }
type Tab = 'inventory' | 'tasks' | 'schedule'

async function api<T>(path: string, authorization: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(path, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      Authorization: `Basic ${authorization}`,
      ...options.headers,
    },
  })
  const payload = response.status === 204 ? null : await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(payload?.message ?? `Request failed with status ${response.status}`)
  }
  return payload as T
}

function Icon({ children }: { children: string }) {
  return <span aria-hidden="true" className="nav-icon">{children}</span>
}

function App() {
  const [authorization, setAuthorization] = useState<string | null>(null)
  const [username, setUsername] = useState('')
  const [companies, setCompanies] = useState<Company[]>([])
  const [branches, setBranches] = useState<Branch[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [stock, setStock] = useState<StockItem[]>([])
  const [tasks, setTasks] = useState<Task[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])
  const [shifts, setShifts] = useState<Shift[]>([])
  const [companyId, setCompanyId] = useState('')
  const [branchId, setBranchId] = useState('')
  const [tab, setTab] = useState<Tab>('inventory')
  const [notice, setNotice] = useState<Notice | null>(null)
  const [busy, setBusy] = useState(false)

  const activeBranch = branches.find((branch) => String(branch.id) === branchId)
  const lowStock = useMemo(() => stock.filter((item) => item.lowStock), [stock])
  const openTasks = useMemo(() => tasks.filter((task) => task.status !== 'DONE' && task.status !== 'CANCELLED'), [tasks])

  useEffect(() => {
    if (!authorization) return
    let current = true
    api<Company[]>('/api/companies', authorization)
      .then((rows) => {
        if (!current) return
        setCompanies(rows)
        if (rows.length && !rows.some((row) => String(row.id) === companyId)) setCompanyId(String(rows[0].id))
      })
      .catch((error: Error) => current && setNotice({ kind: 'error', text: error.message }))
    return () => { current = false }
  }, [authorization, companyId])

  useEffect(() => {
    if (!authorization || !companyId) return
    let current = true
    Promise.all([
      api<Branch[]>(`/api/branches?companyId=${companyId}`, authorization),
      api<Product[]>(`/api/products?companyId=${companyId}`, authorization),
    ]).then(([branchRows, productRows]) => {
      if (!current) return
      setBranches(branchRows)
      setProducts(productRows)
      if (branchRows.length) setBranchId(String(branchRows[0].id))
    }).catch((error: Error) => current && setNotice({ kind: 'error', text: error.message }))
    return () => { current = false }
  }, [authorization, companyId])

  useEffect(() => {
    if (!authorization || !branchId) return
    let current = true
    Promise.all([
      api<StockItem[]>(`/api/branches/${branchId}/stock`, authorization),
      api<Page<Task>>(`/api/branches/${branchId}/tasks?page=0&size=100`, authorization),
      api<Employee[]>(`/api/branches/${branchId}/employees`, authorization),
      api<Shift[]>(`/api/branches/${branchId}/shifts`, authorization),
    ]).then(([stockRows, taskPage, employeeRows, shiftRows]) => {
      if (!current) return
      setStock(stockRows)
      setTasks(taskPage.content)
      setEmployees(employeeRows)
      setShifts(shiftRows)
    }).catch((error: Error) => current && setNotice({ kind: 'error', text: error.message }))
    return () => { current = false }
  }, [authorization, branchId])

  function clearBranchData() {
    setStock([])
    setTasks([])
    setEmployees([])
    setShifts([])
  }

  function chooseCompany(id: string) {
    setCompanyId(id)
    setBranchId('')
    clearBranchData()
  }

  function chooseBranch(id: string) {
    setBranchId(id)
    clearBranchData()
  }

  async function signIn(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const user = String(form.get('username') ?? '').trim()
    const password = String(form.get('password') ?? '')
    setBusy(true)
    setNotice(null)
    try {
      const token = btoa(`${user}:${password}`)
      await api<Company[]>('/api/companies', token)
      setUsername(user)
      setAuthorization(token)
    } catch (error) {
      setNotice({ kind: 'error', text: error instanceof Error ? error.message : 'Unable to sign in' })
    } finally {
      setBusy(false)
    }
  }

  function signOut() {
    setAuthorization(null)
    setUsername('')
    setCompanies([])
    setBranches([])
    setProducts([])
    setStock([])
    setTasks([])
    setEmployees([])
    setShifts([])
    setCompanyId('')
    setBranchId('')
    setNotice(null)
  }

  async function refreshCompanyData(nextCompanyId = companyId) {
    if (!authorization || !nextCompanyId) return
    const [branchRows, productRows] = await Promise.all([
      api<Branch[]>(`/api/branches?companyId=${nextCompanyId}`, authorization),
      api<Product[]>(`/api/products?companyId=${nextCompanyId}`, authorization),
    ])
    setBranches(branchRows)
    setProducts(productRows)
  }

  async function refreshBranchData(nextBranchId = branchId) {
    if (!authorization || !nextBranchId) return
    const [stockRows, taskPage, employeeRows, shiftRows] = await Promise.all([
      api<StockItem[]>(`/api/branches/${nextBranchId}/stock`, authorization),
      api<Page<Task>>(`/api/branches/${nextBranchId}/tasks?page=0&size=100`, authorization),
      api<Employee[]>(`/api/branches/${nextBranchId}/employees`, authorization),
      api<Shift[]>(`/api/branches/${nextBranchId}/shifts`, authorization),
    ])
    setStock(stockRows)
    setTasks(taskPage.content)
    setEmployees(employeeRows)
    setShifts(shiftRows)
  }

  async function perform(action: () => Promise<void>, successMessage: string) {
    setBusy(true)
    setNotice(null)
    try {
      await action()
      setNotice({ kind: 'success', text: successMessage })
    } catch (error) {
      setNotice({ kind: 'error', text: error instanceof Error ? error.message : 'Something went wrong' })
    } finally {
      setBusy(false)
    }
  }

  if (!authorization) {
    return <main className="login-shell">
      <section className="login-card">
        <div className="brand-mark">B</div>
        <p className="eyebrow">BRANCH OPERATIONS</p>
        <h1>Welcome to BranchFlow</h1>
        <p className="muted">Sign in with a demo account to manage branches, inventory, and daily work.</p>
        {notice && <div className={`notice ${notice.kind}`}>{notice.text}</div>}
        <form className="stack-form" onSubmit={signIn}>
          <label>Username<input name="username" autoComplete="username" placeholder="manager" required /></label>
          <label>Password<input name="password" type="password" autoComplete="current-password" required /></label>
          <button className="primary-button" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}<span>→</span></button>
        </form>
        <p className="login-footnote">Credentials are configured in your local <code>.env</code> file.</p>
      </section>
      <aside className="login-art"><div className="art-grid" /><div className="art-copy"><span className="art-kicker">ONE VIEW. EVERY LOCATION.</span><h2>Keep every branch<br />moving forward.</h2><p>Stock, people, and priorities in sync.</p></div><div className="art-orbit orbit-one" /><div className="art-orbit orbit-two" /></aside>
    </main>
  }

  async function createCompany(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const auth = authorization
    if (!auth) return
    const formElement = event.currentTarget
    const name = String(new FormData(event.currentTarget).get('name') ?? '').trim()
    await perform(async () => {
      const created = await api<Company>('/api/companies', auth, { method: 'POST', body: JSON.stringify({ name }) })
      const rows = await api<Company[]>('/api/companies', auth)
      setCompanies(rows)
      setBranchId('')
      clearBranchData()
      setCompanyId(String(created.id))
      formElement.reset()
    }, 'Company created')
  }

  async function createBranch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const auth = authorization
    if (!auth) return
    const formElement = event.currentTarget
    const form = new FormData(event.currentTarget)
    await perform(async () => {
      const created = await api<Branch>('/api/branches', auth, {
        method: 'POST', body: JSON.stringify({ companyId: Number(companyId), name: form.get('name'), city: form.get('city') }),
      })
      await refreshCompanyData()
      setBranchId(String(created.id))
      formElement.reset()
    }, 'Branch added')
  }

  async function createProduct(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const auth = authorization
    if (!auth) return
    const formElement = event.currentTarget
    const form = new FormData(event.currentTarget)
    await perform(async () => {
      await api<Product>('/api/products', auth, {
        method: 'POST', body: JSON.stringify({ companyId: Number(companyId), sku: form.get('sku'), name: form.get('name') }),
      })
      await refreshCompanyData()
      formElement.reset()
    }, 'Product added to the catalog')
  }

  async function createStock(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const auth = authorization
    if (!auth) return
    const formElement = event.currentTarget
    const form = new FormData(event.currentTarget)
    await perform(async () => {
      await api<StockItem>(`/api/branches/${branchId}/stock`, auth, {
        method: 'POST', body: JSON.stringify({ productId: Number(form.get('productId')), minimumQuantity: Number(form.get('minimumQuantity')) }),
      })
      await refreshBranchData()
      formElement.reset()
    }, 'Stock record created')
  }

  async function createMovement(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const auth = authorization
    if (!auth) return
    const formElement = event.currentTarget
    const form = new FormData(event.currentTarget)
    const type = String(form.get('type'))
    const quantity = Number(form.get('quantity'))
    const direction = String(form.get('direction') ?? 'add')
    const quantityChange = type === 'CORRECTION' ? quantity * (direction === 'remove' ? -1 : 1)
      : ['SALE', 'DAMAGE'].includes(type) ? -quantity : quantity
    await perform(async () => {
      await api(`/api/stock/${Number(form.get('stockItemId'))}/movements`, auth, {
        method: 'POST', body: JSON.stringify({ type, quantityChange, reason: form.get('reason') }),
      })
      await refreshBranchData()
      formElement.reset()
    }, 'Stock movement recorded')
  }

  async function transferStock(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const auth = authorization
    if (!auth) return
    const formElement = event.currentTarget
    const form = new FormData(event.currentTarget)
    await perform(async () => {
      await api('/api/stock/transfers', auth, {
        method: 'POST', body: JSON.stringify({
          productId: Number(form.get('productId')), fromBranchId: Number(form.get('fromBranchId')),
          toBranchId: Number(form.get('toBranchId')), quantity: Number(form.get('quantity')), reason: form.get('reason'),
        }),
      })
      await refreshBranchData()
      formElement.reset()
    }, 'Transfer recorded at both branches')
  }

  async function createTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const auth = authorization
    if (!auth) return
    const formElement = event.currentTarget
    const form = new FormData(event.currentTarget)
    const dueAt = String(form.get('dueAt') ?? '')
    await perform(async () => {
      await api<Task>(`/api/branches/${branchId}/tasks`, auth, {
        method: 'POST', body: JSON.stringify({ title: form.get('title'), description: form.get('description'), priority: form.get('priority'), dueAt: dueAt ? new Date(dueAt).toISOString() : null }),
      })
      await refreshBranchData()
      formElement.reset()
    }, 'Task created')
  }

  async function changeTaskStatus(taskId: number, status: string) {
    const auth = authorization
    if (!auth) return
    await perform(async () => {
      await api(`/api/tasks/${taskId}/status`, auth, { method: 'PATCH', body: JSON.stringify({ status }) })
      await refreshBranchData()
    }, 'Task updated')
  }

  async function createEmployee(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const auth = authorization
    if (!auth) return
    const formElement = event.currentTarget
    const form = new FormData(event.currentTarget)
    await perform(async () => {
      await api<Employee>(`/api/branches/${branchId}/employees`, auth, {
        method: 'POST', body: JSON.stringify({ name: form.get('name'), email: form.get('email') }),
      })
      await refreshBranchData()
      formElement.reset()
    }, 'Employee added')
  }

  async function createShift(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const auth = authorization
    if (!auth) return
    const formElement = event.currentTarget
    const form = new FormData(event.currentTarget)
    await perform(async () => {
      await api<Shift>(`/api/branches/${branchId}/shifts`, auth, {
        method: 'POST', body: JSON.stringify({ employeeId: Number(form.get('employeeId')), startTime: new Date(String(form.get('startTime'))).toISOString(), endTime: new Date(String(form.get('endTime'))).toISOString() }),
      })
      await refreshBranchData()
      formElement.reset()
    }, 'Shift scheduled')
  }

  async function cancelShift(shiftId: number) {
    const auth = authorization
    if (!auth) return
    await perform(async () => {
      await api(`/api/shifts/${shiftId}/status`, auth, { method: 'PATCH', body: JSON.stringify({ status: 'CANCELLED' }) })
      await refreshBranchData()
    }, 'Shift cancelled')
  }

  const branchOptions = branches.map((branch) => <option key={branch.id} value={branch.id}>{branch.name} · {branch.city}</option>)

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="sidebar-brand"><div className="brand-mark small">B</div><span>branchflow</span></div>
      <p className="side-label">WORKSPACE</p>
      <nav className="main-nav" aria-label="Main navigation">
        <button className={tab === 'inventory' ? 'active' : ''} onClick={() => setTab('inventory')}><Icon>▦</Icon>Overview & inventory</button>
        <button className={tab === 'tasks' ? 'active' : ''} onClick={() => setTab('tasks')}><Icon>☷</Icon>Tasks<span className="nav-count">{openTasks.length}</span></button>
        <button className={tab === 'schedule' ? 'active' : ''} onClick={() => setTab('schedule')}><Icon>◷</Icon>People & shifts</button>
      </nav>
      <div className="sidebar-bottom"><div className="live-dot" />All systems operational <span>v1.0</span></div>
    </aside>

    <main className="main-area">
      <header className="topbar">
        <div className="breadcrumbs"><span>Workspace</span><b>/</b><strong>{tab === 'inventory' ? 'Overview' : tab === 'tasks' ? 'Tasks' : 'People & shifts'}</strong></div>
        <div className="top-actions">
          <label className="select-wrap"><span className="select-label">COMPANY</span><select value={companyId} onChange={(event) => chooseCompany(event.target.value)} aria-label="Select company"><option value="">Select company</option>{companies.map((company) => <option key={company.id} value={company.id}>{company.name}</option>)}</select></label>
          <label className="select-wrap branch-select"><span className="select-label">BRANCH</span><select value={branchId} onChange={(event) => chooseBranch(event.target.value)} aria-label="Select branch"><option value="">Select branch</option>{branchOptions}</select></label>
          <button className="avatar-button" title={`Signed in as ${username}`} onClick={signOut}>{username.slice(0, 1).toUpperCase()}</button>
        </div>
      </header>

      <div className="page-wrap">
        {notice && <div role="status" className={`notice ${notice.kind}`}><span>{notice.kind === 'success' ? '✓' : '!'}</span>{notice.text}<button onClick={() => setNotice(null)} aria-label="Dismiss notice">×</button></div>}
        {!companies.length && <section className="empty-company"><div className="empty-symbol">＋</div><div><p className="eyebrow">START YOUR WORKSPACE</p><h2>Create your first company</h2><p className="muted">Companies hold the product catalog and group their branches.</p></div><form className="inline-create" onSubmit={createCompany}><input name="name" placeholder="Company name" maxLength={150} required /><button className="primary-button" disabled={busy}>Create company</button></form></section>}
        {companies.length > 0 && !branches.length && <section className="empty-company"><div className="empty-symbol">⌂</div><div><p className="eyebrow">FIRST LOCATION</p><h2>Add a branch</h2><p className="muted">Create the first store location to start managing its operations.</p></div><form className="inline-create" onSubmit={createBranch}><input name="name" placeholder="Branch name" maxLength={100} required /><input name="city" placeholder="City" maxLength={100} required /><button className="primary-button" disabled={busy}>Add branch</button></form></section>}

        {activeBranch && <>
          <section className="page-heading"><div><p className="eyebrow">{activeBranch.city.toUpperCase()} · BRANCH {activeBranch.id.toString().padStart(3, '0')}</p><h1>Good work starts <em>here.</em></h1><p className="muted">A clear view of what your branch needs today.</p></div><button className="outline-button" onClick={() => setNotice({ kind: 'success', text: `Viewing ${activeBranch.name}` })}>⌖ <span>{activeBranch.name}</span></button></section>
          <section className="metric-grid">
            <Metric label="Tracked products" value={stock.length} hint="Across this branch" icon="▤" tone="sage" />
            <Metric label="Needs attention" value={lowStock.length} hint={lowStock.length ? 'At or below minimum stock' : 'Stock levels look healthy'} icon="⌁" tone={lowStock.length ? 'amber' : 'sage'} />
            <Metric label="Open priorities" value={openTasks.length} hint="Tasks still in progress" icon="☷" tone="blue" />
            <Metric label="Upcoming shifts" value={shifts.filter((shift) => shift.status === 'PLANNED').length} hint={`${employees.length} team members`} icon="◷" tone="lavender" />
          </section>

          <div className="section-heading"><div><p className="eyebrow">BRANCH OPERATIONS</p><h2>{tab === 'inventory' ? 'Inventory' : tab === 'tasks' ? 'Task board' : 'People & shifts'}</h2></div><div className="tab-pills"><button className={tab === 'inventory' ? 'selected' : ''} onClick={() => setTab('inventory')}>Inventory</button><button className={tab === 'tasks' ? 'selected' : ''} onClick={() => setTab('tasks')}>Tasks</button><button className={tab === 'schedule' ? 'selected' : ''} onClick={() => setTab('schedule')}>Schedule</button></div></div>

          {tab === 'inventory' && <>
            <section className="content-grid inventory-grid">
              <div className="panel stock-panel"><div className="panel-heading"><div><p className="eyebrow">LIVE STOCK</p><h3>Product levels</h3></div><span className="soft-badge">{stock.length} items</span></div>
                {stock.length ? <div className="table-scroll"><table><thead><tr><th>PRODUCT</th><th>SKU</th><th>ON HAND</th><th>MINIMUM</th><th>STATUS</th></tr></thead><tbody>{stock.map((item) => <tr key={item.id}><td><strong>{item.productName}</strong></td><td className="mono">{item.sku}</td><td className="quantity-cell">{item.quantity}</td><td>{item.minimumQuantity}</td><td><span className={`status-pill ${item.lowStock ? 'warning' : 'good'}`}><i />{item.lowStock ? 'Restock' : 'Healthy'}</span></td></tr>)}</tbody></table></div> : <EmptyMessage title="No stock records yet" body="Add a product to this branch to begin tracking stock." />}
              </div>
              <div className="panel low-panel"><div className="panel-heading"><div><p className="eyebrow">REPLENISHMENT</p><h3>Low stock watch</h3></div><span className="count-orb">{lowStock.length}</span></div>{lowStock.length ? lowStock.map((item) => <div className="low-row" key={item.id}><span className="product-mark">{item.productName.slice(0, 1)}</span><div><strong>{item.productName}</strong><small>{item.quantity} units left · minimum {item.minimumQuantity}</small></div><span className="low-tag">LOW</span></div>) : <EmptyMessage title="All clear" body="No products are below their minimum quantity." />}</div>
            </section>
            <section className="form-grid three-columns">
              <FormPanel eyebrow="CATALOG" title="Add a product" onSubmit={createProduct} busy={busy} button="Add product"><label>Product name<input name="name" placeholder="Cola 1 L" maxLength={150} required /></label><label>SKU<input name="sku" placeholder="COLA-1L" maxLength={64} required /></label></FormPanel>
              <FormPanel eyebrow="BRANCH STOCK" title="Track a product" onSubmit={createStock} busy={busy} button="Add to stock"><label>Product<select name="productId" required defaultValue=""><option value="" disabled>Select product</option>{products.map((product) => <option value={product.id} key={product.id}>{product.name} · {product.sku}</option>)}</select></label><label>Minimum quantity<input name="minimumQuantity" type="number" min="0" defaultValue="5" required /></label></FormPanel>
              <FormPanel eyebrow="AUDIT LEDGER" title="Record movement" onSubmit={createMovement} busy={busy} button="Record movement"><label>Stock item<select name="stockItemId" required defaultValue=""><option value="" disabled>Select product</option>{stock.map((item) => <option value={item.id} key={item.id}>{item.productName} · {item.quantity} on hand</option>)}</select></label><div className="split-fields"><label>Movement<select name="type"><option value="DELIVERY">Delivery</option><option value="SALE">Sale</option><option value="DAMAGE">Damage</option><option value="CORRECTION">Correction</option></select></label><label>Quantity<input name="quantity" type="number" min="1" defaultValue="1" required /></label></div><label className="correction-field">Correction direction<select name="direction"><option value="add">Increase</option><option value="remove">Decrease</option></select></label><label>Reason<input name="reason" placeholder="Supplier delivery" maxLength={300} required /></label></FormPanel>
            </section>
            {branches.length > 1 && <section className="form-grid transfer-grid"><FormPanel eyebrow="BETWEEN LOCATIONS" title="Transfer stock" onSubmit={transferStock} busy={busy} button="Transfer inventory"><div className="split-fields"><label>From<select name="fromBranchId" defaultValue={branchId}>{branches.map((branch) => <option value={branch.id} key={branch.id}>{branch.name}</option>)}</select></label><label>To<select name="toBranchId" defaultValue={branches.find((branch) => String(branch.id) !== branchId)?.id}>{branches.filter((branch) => String(branch.id) !== branchId).map((branch) => <option value={branch.id} key={branch.id}>{branch.name}</option>)}</select></label></div><div className="split-fields"><label>Product<select name="productId" required defaultValue=""><option value="" disabled>Select product</option>{products.map((product) => <option value={product.id} key={product.id}>{product.name}</option>)}</select></label><label>Quantity<input name="quantity" type="number" min="1" defaultValue="1" required /></label></div><label>Reason<input name="reason" placeholder="Rebalance for weekend demand" maxLength={300} required /></label></FormPanel></section>}
          </>}

          {tab === 'tasks' && <section className="content-grid task-layout"><FormPanel eyebrow="NEW PRIORITY" title="Create a task" onSubmit={createTask} busy={busy} button="Create task"><label>Task title<input name="title" placeholder="Restock front display" maxLength={160} required /></label><label>Description<textarea name="description" placeholder="Add helpful detail for the team" maxLength={4000} rows={3} /></label><div className="split-fields"><label>Priority<select name="priority"><option value="NORMAL">Normal</option><option value="LOW">Low</option><option value="HIGH">High</option><option value="URGENT">Urgent</option></select></label><label>Due date<input name="dueAt" type="datetime-local" /></label></div></FormPanel><div className="panel task-panel"><div className="panel-heading"><div><p className="eyebrow">TEAM WORK</p><h3>Branch tasks</h3></div><span className="soft-badge">{tasks.length} total</span></div>{tasks.length ? <div className="task-list">{tasks.map((task) => <article className="task-row" key={task.id}><div className={`task-priority ${task.priority.toLowerCase()}`} /><div className="task-content"><div className="task-title-line"><strong>{task.title}</strong><span className={`priority-tag ${task.priority.toLowerCase()}`}>{task.priority}</span></div>{task.description && <p>{task.description}</p>}<small>{task.dueAt ? `Due ${new Date(task.dueAt).toLocaleString()}` : 'No due date'} · Added {new Date(task.createdAt).toLocaleDateString()}</small></div><select aria-label={`Status for ${task.title}`} value={task.status} onChange={(event) => void changeTaskStatus(task.id, event.target.value)}><option value="OPEN">Open</option><option value="IN_PROGRESS">In progress</option><option value="DONE">Done</option><option value="CANCELLED">Cancelled</option></select></article>)}</div> : <EmptyMessage title="A clean slate" body="Create a task to give the team a clear next step." />}</div></section>}

          {tab === 'schedule' && <section className="schedule-layout"><div className="form-grid two-columns"><FormPanel eyebrow="TEAM DIRECTORY" title="Add an employee" onSubmit={createEmployee} busy={busy} button="Add employee"><label>Full name<input name="name" placeholder="Alex Example" maxLength={120} required /></label><label>Email address<input name="email" type="email" placeholder="alex@company.com" maxLength={254} required /></label></FormPanel><FormPanel eyebrow="SHIFT PLANNING" title="Schedule a shift" onSubmit={createShift} busy={busy || employees.length === 0} button="Schedule shift"><label>Employee<select name="employeeId" required defaultValue=""><option value="" disabled>{employees.length ? 'Select employee' : 'Add an employee first'}</option>{employees.map((employee) => <option value={employee.id} key={employee.id}>{employee.name}</option>)}</select></label><div className="split-fields"><label>Starts<input name="startTime" type="datetime-local" required /></label><label>Ends<input name="endTime" type="datetime-local" required /></label></div><p className="form-hint">Overlapping shifts for the same employee are automatically rejected.</p></FormPanel></div><div className="content-grid schedule-panels"><div className="panel"><div className="panel-heading"><div><p className="eyebrow">PEOPLE</p><h3>Branch team</h3></div><span className="soft-badge">{employees.length} people</span></div>{employees.length ? employees.map((employee) => <div className="person-row" key={employee.id}><span className="person-avatar">{employee.name.split(' ').map((part) => part[0]).slice(0, 2).join('').toUpperCase()}</span><div><strong>{employee.name}</strong><small>{employee.email}</small></div></div>) : <EmptyMessage title="No team members yet" body="Add employees to start scheduling shifts." />}</div><div className="panel"><div className="panel-heading"><div><p className="eyebrow">COVERAGE</p><h3>Upcoming shifts</h3></div><span className="soft-badge">{shifts.length} planned</span></div>{shifts.length ? shifts.map((shift) => <div className="shift-row" key={shift.id}><div className="shift-time"><strong>{new Date(shift.startTime).toLocaleDateString([], { month: 'short', day: 'numeric' })}</strong><small>{new Date(shift.startTime).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })} – {new Date(shift.endTime).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })}</small></div><div className="shift-person"><span className={`person-avatar ${shift.status === 'CANCELLED' ? 'faded' : ''}`}>{shift.employeeName.slice(0, 1)}</span><div><strong>{shift.employeeName}</strong><small>{shift.status.replace('_', ' ')}</small></div></div>{shift.status === 'PLANNED' && <button className="text-button" onClick={() => void cancelShift(shift.id)}>Cancel</button>}</div>) : <EmptyMessage title="No shifts scheduled" body="Plan the first shift to build your branch schedule." />}</div></div></section>}
        </>}
      </div>
    </main>
  </div>
}

function Metric({ label, value, hint, icon, tone }: { label: string; value: number; hint: string; icon: string; tone: string }) {
  return <article className="metric-card"><span className={`metric-icon ${tone}`}>{icon}</span><span className="metric-label">{label}</span><strong className="metric-value">{value.toString().padStart(2, '0')}</strong><span className="metric-hint">{hint}</span></article>
}

function FormPanel({ eyebrow, title, onSubmit, busy, button, children }: {
  eyebrow: string; title: string; onSubmit: (event: FormEvent<HTMLFormElement>) => void;
  busy: boolean; button: string; children: ReactNode
}) {
  return <form className="panel form-panel" onSubmit={onSubmit}><div className="panel-heading"><div><p className="eyebrow">{eyebrow}</p><h3>{title}</h3></div></div><div className="form-fields">{children}</div><button className="primary-button full-button" disabled={busy}>{busy ? 'Please wait…' : button}<span>→</span></button></form>
}

function EmptyMessage({ title, body }: { title: string; body: string }) {
  return <div className="empty-message"><span>✳</span><strong>{title}</strong><p>{body}</p></div>
}

export default App
