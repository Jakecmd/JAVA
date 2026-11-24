import React, { useState } from 'react'

interface ScreenshotItem {
  id: number
  filename: string
  src: string
  title: string
  point: string
  detail: string
}

const SCREENSHOTS: ScreenshotItem[] = [
  {
    id: 1,
    filename: '图1 登录页面.png',
    src: './deliverables/screenshots/shot1.png',
    title: '图1 登录页面',
    point: '对应要求：login.html 页面开发（4分）',
    detail: '访问 /bookstore/login.html 展示用户名、密码、【记住我的用户名（7天）】复选框与 POST 提交按钮。'
  },
  {
    id: 2,
    filename: '图2 记住用户名回显效果.png',
    src: './deliverables/screenshots/shot2.png',
    title: '图2 记住用户名回显效果',
    point: '对应要求：持久 Cookie 7天存活与自动回显（23分）',
    detail: '勾选记住用户名登录后关闭重开浏览器，JSESSIONID 消失而 7 天持久 Cookie username 保留，自动回显 admin。'
  },
  {
    id: 3,
    filename: '图3 购物车页面.png',
    src: './deliverables/screenshots/shot3.png',
    title: '图3 购物车页面',
    point: '对应要求：HttpSession 存储购物车并累积多本图书（23分）',
    detail: '多次点击加入购物车累积 4 本图书（同书数量累加、不同书并列展示），显示 SessionID 与 Cookie 传递方式。'
  },
  {
    id: 4,
    filename: '图4 控制台打印sessionId.png',
    src: './deliverables/screenshots/shot4.png',
    title: '图4 控制台打印 sessionId',
    point: '对应要求：AddCartServlet 控制台打印 sessionId 验证不变（23分）',
    detail: 'Tomcat 9 控制台真实输出：同一会话连续 4 次加购打印相同的 sessionId（FDCECA8C50B4F64D98D401B6C1C740E1），以及退出销毁日志。'
  },
  {
    id: 5,
    filename: '图5 禁用Cookie后URL携带jsessionid.png',
    src: './deliverables/screenshots/shot5.png',
    title: '图5 禁用 Cookie 后 URL 携带 jsessionid',
    point: '对应要求：encodeURL / encodeRedirectURL URL重写（10分）',
    detail: '浏览器禁用 Cookie 后登录并加购，地址栏自动拼接 ;jsessionid=63EF3355...，会话正常维持，购物车正常累积。'
  },
  {
    id: 6,
    filename: '图6 退出登录效果.png',
    src: './deliverables/screenshots/shot6.png',
    title: '图6 退出登录效果',
    point: '对应要求：session.invalidate() 销毁会话 + setMaxAge(0) 删除 Cookie',
    detail: '点击安全退出后销毁 Session、删除 username Cookie，通过 encodeRedirectURL 重定向回登录页并清空用户名输入框。'
  }
]

const FIX_ITEMS = [
  {
    file: 'LoginServlet.java（doGet 与 doPost）',
    issue: '1. 原 doGet() 在用户已登录时直接 302 跳回 /bookList，导致登录状态下无法直接访问 /bookstore/login 验证服务器端遍历 Cookie[] 回显逻辑；2. 原 doPost() 登录失败转发到静态 login.html 前调用了无效的 request.setAttribute("msg", ...)，静态 HTML 无法读取 request 属性。',
    fix: '1. 优化 doGet()：已登录时显示顶部快捷入口提示条，同时继续执行 request.getCookies() 遍历并将用户名回显到输入框；2. 移除指向静态页的无效 setAttribute 调用并补充清晰注释，保持登录失败使用 forward 请求转发。'
  },
  {
    file: 'login.html（Cookie 读取脚本）',
    issue: '原 getCookie() 使用 split("; ") 切分且未检查 indexOf("=") 返回 -1 的边界情况，在部分浏览器无空格分隔或空键值 Cookie 下不够稳健。',
    fix: '改为正则 split(/;\\s*/) 兼容不同浏览器格式，并增加 pos > 0 边界校验，确保中文 URL 编码及特殊字符用户名稳定回显。'
  },
  {
    file: 'BookDao.java / UserDao.java / DBUtil.java',
    issue: '1. 若本机 MySQL 连接成功但 t_book 表为空（0 行），原代码直接返回空列表导致书籍列表页空白无法加购；2. 若上次实验库表名不一致导致 SQL 报错，未将 DBUtil 标记降级，后续每次请求都会重复连接报错。',
    fix: '1. 当查询结果为空表时自动回退使用内置 6 本模拟图书数据；2. 新增 DBUtil.markUnavailable() 在表不存在时自动降级，并将 db.properties 的 connectTimeout 缩短至 1500ms，无数据库环境下首次响应更快。'
  },
  {
    file: 'ShowCartServlet.java',
    issue: '当购物车为空时页面底部仍显示“清空购物车”按钮，点击会弹出多余确认框并发起无意义的清空请求。',
    fix: '增加空车判断，仅在购物车非空时渲染“清空购物车”（session.removeAttribute）按钮，页面所有超链接均保持 response.encodeURL() 重写。'
  },
  {
    file: '电子书店会话跟踪实验报告.docx & 运行截图/',
    issue: '原运行截图目录仅有清单说明无截图文件，Word 实验报告中 6 处【在此处粘贴截图】尚未贴图。',
    fix: '在 Apache Tomcat 9.0.124 上真实编译部署项目，通过 Chromium 完成全部 6 组实验测试并截取高清截图，已保存至 运行截图/ 目录并直接嵌入 Word 实验报告对应位置，同步更新了报告中的代码片段。'
  }
]

const BOOKS = [
  { id: 1, name: 'Java核心技术 卷I', author: 'Cay S. Horstmann', publisher: '机械工业出版社', price: 119.0 },
  { id: 2, name: 'Head First Servlets & JSP', author: 'Bryan Basham', publisher: '中国电力出版社', price: 98.0 },
  { id: 3, name: '深入理解Java虚拟机', author: '周志明', publisher: '机械工业出版社', price: 129.0 },
  { id: 4, name: 'Effective Java（第3版）', author: 'Joshua Bloch', publisher: '机械工业出版社', price: 119.0 },
  { id: 5, name: '图解HTTP', author: '上野宣', publisher: '人民邮电出版社', price: 49.0 },
  { id: 6, name: 'MySQL必知必会', author: 'Ben Forta', publisher: '人民邮电出版社', price: 39.0 }
]

function DownloadIcon() {
  return (
    <svg className="w-4 h-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v2a2 2 0 002 2h12a2 2 0 002-2v-2M7 10l5 5m0 0l5-5m-5 5V3" />
    </svg>
  )
}

function CheckIcon() {
  return (
    <svg className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4">
      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
    </svg>
  )
}

export default function App() {
  const [activeTab, setActiveTab] = useState<'shots' | 'review' | 'demo'>('shots')
  const [lightbox, setLightbox] = useState<ScreenshotItem | null>(null)

  // Interactive demo state
  const [cookieDisabled, setCookieDisabled] = useState(false)
  const [savedCookie, setSavedCookie] = useState<{ value: string; persistent: boolean } | null>({
    value: 'admin',
    persistent: true
  })
  const [sessionId, setSessionId] = useState<string>('FDCECA8C50B4F64D98D401B6C1C740E1')
  const [loginUser, setLoginUser] = useState<string | null>(null)
  const [demoPage, setDemoPage] = useState<'login' | 'bookList' | 'showCart'>('login')
  const [formUser, setFormUser] = useState('admin')
  const [formPwd, setFormPwd] = useState('123456')
  const [rememberMe, setRememberMe] = useState(true)
  const [banner, setBanner] = useState<{ type: 'error' | 'success' | 'info'; text: string } | null>({
    type: 'info',
    text: '已从 Cookie 中读取到用户名：admin'
  })
  const [cart, setCart] = useState<Record<number, number>>({})
  const [logs, setLogs] = useState<string[]>([
    '[Tomcat 9.0] bookstore:war exploded 已启动，上下文路径 /bookstore'
  ])

  const randomHexSession = () =>
    Array.from({ length: 32 }, () => Math.floor(Math.random() * 16).toString(16).toUpperCase()).join('')

  const appendLog = (line: string) => setLogs((prev) => [...prev, line])

  const currentDemoUrl = () => {
    const suffix = cookieDisabled && loginUser ? `;jsessionid=${sessionId}` : ''
    if (demoPage === 'login') return 'http://localhost:8080/bookstore/login.html'
    if (demoPage === 'bookList') return `http://localhost:8080/bookstore/bookList${suffix}`
    return `http://localhost:8080/bookstore/showCart${suffix}`
  }

  const handleDemoLogin = (e: React.FormEvent) => {
    e.preventDefault()
    const u = formUser.trim()
    const valid =
      (u === 'admin' && formPwd === '123456') ||
      (u === '张三' && formPwd === '123456') ||
      (u === 'lisi' && formPwd === '654321')
    if (!valid) {
      setBanner({ type: 'error', text: '用户名或密码错误，请重新输入！（请求转发 forward 回 /login，地址栏不变）' })
      appendLog(`[LoginServlet] 登录失败：用户=${u}，执行 forward("/login.html")`)
      return
    }
    const sid = loginUser ? sessionId : randomHexSession()
    setSessionId(sid)
    setLoginUser(u)
    if (!cookieDisabled) {
      setSavedCookie({ value: u, persistent: rememberMe })
    }
    setBanner(null)
    setDemoPage('bookList')
    appendLog(`[SessionListener] 会话就绪 sessionId=${sid}`)
    appendLog(
      `[LoginServlet] 登录成功：用户=${u}，sessionId=${sid}，记住用户名=${
        rememberMe ? '是（持久Cookie 7天）' : '否（临时Cookie）'
      }`
    )
  }

  const handleDemoAddCart = (bookId: number) => {
    const book = BOOKS.find((b) => b.id === bookId)!
    const next = { ...cart, [bookId]: (cart[bookId] || 0) + 1 }
    const totalCount = Object.values(next).reduce((a, b) => a + b, 0)
    setCart(next)
    appendLog(`[AddCartServlet] sessionId=${sessionId} | 用户=${loginUser} | 加入《${book.name}》 | 购物车共 ${totalCount} 本`)
    setDemoPage('showCart')
  }

  const handleDemoLogout = () => {
    appendLog(`[LogoutServlet] 用户 ${loginUser} 安全退出，session.invalidate() 已销毁会话 sessionId=${sessionId}`)
    setLoginUser(null)
    setCart({})
    setSavedCookie(null)
    setFormUser('')
    setFormPwd('')
    setDemoPage('login')
    setBanner({ type: 'success', text: '您已安全退出：会话已销毁，用户名 Cookie 已删除（setMaxAge(0)）。' })
  }

  const handleSimulateBrowserRestart = () => {
    appendLog('[浏览器模拟] 关闭并重新打开浏览器：临时会话 JSESSIONID 已失效')
    setLoginUser(null)
    setCart({})
    setDemoPage('login')
    if (!cookieDisabled && savedCookie?.persistent) {
      setFormUser(savedCookie.value)
      setBanner({ type: 'info', text: `已从 7 天持久 Cookie 中读取到用户名：${savedCookie.value}` })
    } else {
      setSavedCookie(null)
      setFormUser('')
      setBanner({ type: 'info', text: '临时 Cookie 已随浏览器关闭失效，用户名输入框未回显。' })
    }
  }

  const totalCount = Object.values(cart).reduce((a, b) => a + b, 0)
  const totalPrice = Object.entries(cart).reduce((sum, [id, qty]) => {
    const b = BOOKS.find((item) => item.id === Number(id))
    return sum + (b ? b.price * qty : 0)
  }, 0)

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      {/* Header */}
      <header className="bg-[#1f4e79] text-white border-b border-slate-800/20">
        <div className="max-w-6xl mx-auto px-6 py-6 flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-2 text-xs font-medium bg-white/15 px-3 py-1 rounded-full mb-2">
              <span>Tomcat 9.0 编译验证通过</span>
              <span>·</span>
              <span>6 张运行截图已生成并嵌入报告</span>
            </div>
            <h1 className="text-2xl font-bold tracking-tight">
              Java Web 实验：基于会话跟踪技术的简易电子书店购物系统
            </h1>
            <p className="text-sm text-blue-100 mt-1">
              涵盖 HTTP 无状态 · Cookie（7天持久/临时/删除） · HttpSession（购物车/拦截/销毁） · URL 重写（;jsessionid=）
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <a
              href="./deliverables/screenshots_all.zip"
              download="运行截图（全6张）.zip"
              className="inline-flex items-center gap-2 bg-white text-[#1f4e79] hover:bg-blue-50 font-semibold text-sm px-4 py-2.5 rounded-lg shadow-sm transition"
            >
              <DownloadIcon />
              <span>下载全部 6 张截图 (.zip)</span>
            </a>
            <a
              href="./deliverables/report.docx"
              download="电子书店会话跟踪实验报告.docx"
              className="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-sm px-4 py-2.5 rounded-lg shadow-sm transition"
            >
              <DownloadIcon />
              <span>下载嵌好截图的报告 (.docx)</span>
            </a>
            <a
              href="./deliverables/submission.zip"
              download="学号-姓名-电子书店会话跟踪实验.zip"
              className="inline-flex items-center gap-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold text-sm px-4 py-2.5 rounded-lg shadow-sm transition"
            >
              <DownloadIcon />
              <span>下载完整提交压缩包 (.zip)</span>
            </a>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="max-w-6xl mx-auto px-6 flex gap-1">
          {[
            { key: 'shots', label: '运行截图预览与单张下载（6张）' },
            { key: 'review', label: '代码审查与完善记录（对照100分标准）' },
            { key: 'demo', label: '电子书店在线交互演示' }
          ].map((t) => (
            <button
              key={t.key}
              onClick={() => setActiveTab(t.key as 'shots' | 'review' | 'demo')}
              className={`px-4 py-2.5 text-sm font-medium rounded-t-lg transition cursor-pointer ${
                activeTab === t.key
                  ? 'bg-slate-50 text-[#1f4e79] font-semibold'
                  : 'text-blue-100 hover:bg-white/10'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-6 py-8">
        {/* TAB 1: SCREENSHOTS */}
        {activeTab === 'shots' && (
          <div className="space-y-6">
            <div className="bg-white border border-slate-200 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
              <div className="text-sm text-slate-700">
                以下 6 张截图均由项目在 <b>Apache Tomcat 9.0.124</b> 真实部署运行后通过 Chromium 实测截取，已自动写入
                <code className="mx-1 px-1.5 py-0.5 bg-slate-100 rounded text-xs">运行截图/</code>
                文件夹并嵌入《电子书店会话跟踪实验报告.docx》。点击任意图片可放大查看原图。
              </div>
              <span className="text-xs font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 px-3 py-1 rounded-full shrink-0">
                全部符合提交截图要求
              </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {SCREENSHOTS.map((item) => (
                <div
                  key={item.id}
                  className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-xs hover:shadow-md transition flex flex-col"
                >
                  <div
                    onClick={() => setLightbox(item)}
                    className="bg-slate-100 border-b border-slate-200 cursor-zoom-in relative group overflow-hidden"
                  >
                    <img
                      src={item.src}
                      alt={item.title}
                      className="w-full h-64 object-contain object-top bg-slate-100 group-hover:scale-[1.01] transition"
                    />
                    <span className="absolute bottom-2.5 right-2.5 bg-slate-900/75 text-white text-xs px-2.5 py-1 rounded-md opacity-0 group-hover:opacity-100 transition">
                      点击放大查看高清原图
                    </span>
                  </div>
                  <div className="p-4 flex-1 flex flex-col justify-between gap-3">
                    <div>
                      <div className="flex items-center justify-between gap-2">
                        <h3 className="font-bold text-base text-slate-900">{item.title}</h3>
                        <span className="text-xs bg-blue-50 text-[#1f4e79] border border-blue-200 px-2 py-0.5 rounded">
                          {item.point}
                        </span>
                      </div>
                      <p className="text-xs text-slate-600 mt-1.5 leading-relaxed">{item.detail}</p>
                    </div>
                    <div className="pt-2 border-t border-slate-100 flex items-center justify-between">
                      <code className="text-xs text-slate-500">{item.filename}</code>
                      <a
                        href={item.src}
                        download={item.filename}
                        className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#2e75b6] hover:text-[#1f4e79]"
                      >
                        <DownloadIcon />
                        <span>下载此截图</span>
                      </a>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* TAB 2: CODE REVIEW */}
        {activeTab === 'review' && (
          <div className="space-y-6">
            <div className="bg-white border border-slate-200 rounded-xl p-6">
              <h2 className="text-lg font-bold text-slate-900 mb-3">一、评分细则逐项核查结果（总分 100 分全覆盖）</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                {[
                  '页面开发（4分）：login.html 含用户名、密码、记住我的用户名复选框、POST提交、JS读取Cookie自动回显',
                  'Cookie 功能（23分）：勾选设 setMaxAge(7*24*60*60) 持久 Cookie，未勾选设临时 Cookie，退出 setMaxAge(0) 删除，支持中文 URL 编解码',
                  'HttpSession（23分）：涵盖 getSession() 与 getSession(false) 两种获取方式，存/取/删属性，未登录重定向拦截，控制台打印 sessionId，退出 invalidate()',
                  'URL 重写（10分）：超链接全部调用 encodeURL()，重定向全部调用 encodeRedirectURL()，禁用 Cookie 下自动拼接 ;jsessionid= 正常维持会话',
                  '页面跳转与乱码（10分）：POST 首行 setCharacterEncoding("UTF-8")，登录失败 forward 转发，登录成功与退出 sendRedirect 重定向',
                  '注释、测试与报告（30分）：关键代码注释完整，思考题 1~4 解答详尽，6 张运行截图已生成并嵌入实验报告'
                ].map((line, i) => (
                  <div key={i} className="flex items-start gap-2.5 bg-slate-50 border border-slate-200/80 rounded-lg p-3">
                    <CheckIcon />
                    <span className="text-slate-700 leading-relaxed">{line}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-white border border-slate-200 rounded-xl p-6">
              <h2 className="text-lg font-bold text-slate-900 mb-4">二、本次检查发现并为你修复完善的 5 处细节</h2>
              <div className="space-y-4">
                {FIX_ITEMS.map((item, idx) => (
                  <div key={idx} className="border border-slate-200 rounded-lg p-4">
                    <div className="font-semibold text-[#1f4e79] text-sm mb-2">
                      {idx + 1}. {item.file}
                    </div>
                    <div className="text-xs text-slate-600 space-y-1.5 leading-relaxed">
                      <p>
                        <span className="font-semibold text-amber-700">原问题隐患：</span>
                        {item.issue}
                      </p>
                      <p>
                        <span className="font-semibold text-emerald-700">已修改完善：</span>
                        {item.fix}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: INTERACTIVE DEMO */}
        {activeTab === 'demo' && (
          <div className="space-y-5">
            {/* Simulation Toolbar */}
            <div className="bg-white border border-slate-200 rounded-xl p-4 flex flex-wrap items-center justify-between gap-3">
              <div className="flex flex-wrap items-center gap-4 text-sm">
                <label className="inline-flex items-center gap-2 font-medium text-slate-700 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={cookieDisabled}
                    onChange={(e) => setCookieDisabled(e.target.checked)}
                    className="rounded border-slate-300"
                  />
                  <span>模拟浏览器禁用 Cookie（观察地址栏自动拼接 ;jsessionid=）</span>
                </label>
                <button
                  onClick={handleSimulateBrowserRestart}
                  className="text-xs font-medium bg-slate-100 hover:bg-slate-200 text-slate-800 px-3 py-1.5 rounded-md border border-slate-300 cursor-pointer"
                >
                  模拟关闭并重新打开浏览器（测试持久 vs 临时 Cookie）
                </button>
              </div>
              <div className="text-xs text-slate-500">测试账号：admin / 123456 或 张三 / 123456</div>
            </div>

            {/* Simulated Browser Frame */}
            <div className="bg-white border border-slate-300 rounded-xl overflow-hidden shadow-xs">
              <div className="bg-slate-100 border-b border-slate-200 px-4 py-2.5 flex items-center gap-3">
                <span className="text-xs font-semibold text-slate-500">地址栏 URL</span>
                <div className="flex-1 bg-white border border-slate-200 rounded-full px-3.5 py-1 text-xs font-mono text-slate-800 truncate">
                  {currentDemoUrl()}
                </div>
                {cookieDisabled && (
                  <span className="text-xs bg-red-50 text-red-700 border border-red-200 px-2.5 py-0.5 rounded-full font-medium">
                    Cookie 已禁用
                  </span>
                )}
              </div>

              <div className="p-6 bg-[#f3f5f8]">
                {demoPage === 'login' && (
                  <div className="max-w-sm mx-auto my-6 bg-white p-7 rounded-lg shadow-sm border border-slate-200/80">
                    <h2 className="text-lg font-bold text-[#1f4e79] border-l-4 border-[#2e75b6] pl-2.5 mb-4">
                      用户登录
                    </h2>
                    {banner && (
                      <div
                        className={`text-xs p-2.5 rounded mb-4 border ${
                          banner.type === 'error'
                            ? 'bg-red-50 text-red-700 border-red-200'
                            : banner.type === 'success'
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                            : 'bg-blue-50 text-[#1f4e79] border-blue-200'
                        }`}
                      >
                        {banner.text}
                      </div>
                    )}
                    <form onSubmit={handleDemoLogin} className="space-y-3.5 text-sm">
                      <div>
                        <label className="block text-slate-700 mb-1">用户名：</label>
                        <input
                          type="text"
                          value={formUser}
                          onChange={(e) => setFormUser(e.target.value)}
                          placeholder="请输入用户名"
                          required
                          className="w-full border border-slate-300 rounded px-3 py-1.5 text-sm"
                        />
                      </div>
                      <div>
                        <label className="block text-slate-700 mb-1">密　码：</label>
                        <input
                          type="password"
                          value={formPwd}
                          onChange={(e) => setFormPwd(e.target.value)}
                          placeholder="请输入密码"
                          required
                          className="w-full border border-slate-300 rounded px-3 py-1.5 text-sm"
                        />
                      </div>
                      <label className="flex items-center gap-2 text-xs text-slate-600 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={rememberMe}
                          onChange={(e) => setRememberMe(e.target.checked)}
                        />
                        <span>记住我的用户名（7天）</span>
                      </label>
                      <button
                        type="submit"
                        className="w-full bg-[#2e75b6] hover:bg-[#1f4e79] text-white font-medium py-2 rounded cursor-pointer transition"
                      >
                        登　录
                      </button>
                    </form>
                    <p className="text-xs text-slate-400 text-center mt-3">
                      测试账号：admin / 123456（或 张三 / 123456）
                    </p>
                  </div>
                )}

                {demoPage !== 'login' && loginUser && (
                  <div className="space-y-4">
                    <div className="bg-[#1f4e79] text-white px-4 py-3 rounded-md flex flex-wrap items-center gap-4 text-sm">
                      <span className="font-bold text-base mr-auto">简易电子书店</span>
                      <span>
                        欢迎您，<b>{loginUser}</b>
                      </span>
                      <button onClick={() => setDemoPage('bookList')} className="hover:underline cursor-pointer">
                        书籍列表
                      </button>
                      <button onClick={() => setDemoPage('showCart')} className="hover:underline cursor-pointer">
                        我的购物车（{totalCount}）
                      </button>
                      <button
                        onClick={handleDemoLogout}
                        className="bg-[#c0392b] px-3 py-1 rounded text-xs font-medium cursor-pointer"
                      >
                        安全退出
                      </button>
                    </div>

                    <div className="bg-[#fffbe6] border border-[#f0d58c] rounded-md px-4 py-2.5 text-xs leading-6">
                      <div>
                        <b>当前 SessionID：</b>
                        <code className="text-[#c0392b]">{sessionId}</code>
                      </div>
                      <div>
                        <b>SessionID 传递方式：</b>
                        {cookieDisabled ? 'URL 重写（地址中携带 ;jsessionid=）' : 'Cookie（请求头携带 JSESSIONID）'}
                      </div>
                    </div>

                    {demoPage === 'bookList' && (
                      <div>
                        <h3 className="text-base font-bold text-[#1f4e79] border-l-4 border-[#2e75b6] pl-2.5 mb-3">
                          书籍列表
                        </h3>
                        <div className="overflow-x-auto bg-white rounded border border-slate-200">
                          <table className="w-full text-sm text-center border-collapse">
                            <thead>
                              <tr className="bg-[#2e75b6] text-white">
                                <th className="p-2.5 border border-slate-200">编号</th>
                                <th className="p-2.5 border border-slate-200">书名</th>
                                <th className="p-2.5 border border-slate-200">作者</th>
                                <th className="p-2.5 border border-slate-200">出版社</th>
                                <th className="p-2.5 border border-slate-200">单价</th>
                                <th className="p-2.5 border border-slate-200">操作</th>
                              </tr>
                            </thead>
                            <tbody>
                              {BOOKS.map((b, i) => (
                                <tr key={b.id} className={i % 2 === 1 ? 'bg-slate-50' : 'bg-white'}>
                                  <td className="p-2 border border-slate-200">{b.id}</td>
                                  <td className="p-2 border border-slate-200">《{b.name}》</td>
                                  <td className="p-2 border border-slate-200">{b.author}</td>
                                  <td className="p-2 border border-slate-200">{b.publisher}</td>
                                  <td className="p-2 border border-slate-200 text-[#c0392b] font-bold">
                                    ￥{b.price.toFixed(2)}
                                  </td>
                                  <td className="p-2 border border-slate-200">
                                    <button
                                      onClick={() => handleDemoAddCart(b.id)}
                                      className="bg-[#2e75b6] hover:bg-[#1f4e79] text-white text-xs px-3 py-1.5 rounded cursor-pointer"
                                    >
                                      加入购物车
                                    </button>
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    )}

                    {demoPage === 'showCart' && (
                      <div>
                        <h3 className="text-base font-bold text-[#1f4e79] border-l-4 border-[#2e75b6] pl-2.5 mb-3">
                          我的购物车
                        </h3>
                        {totalCount === 0 ? (
                          <div className="bg-blue-50 border border-blue-200 text-[#1f4e79] text-sm p-3 rounded">
                            购物车还是空的，快去挑选图书吧！
                          </div>
                        ) : (
                          <div className="overflow-x-auto bg-white rounded border border-slate-200">
                            <table className="w-full text-sm text-center border-collapse">
                              <thead>
                                <tr className="bg-[#2e75b6] text-white">
                                  <th className="p-2.5 border border-slate-200">编号</th>
                                  <th className="p-2.5 border border-slate-200">书名</th>
                                  <th className="p-2.5 border border-slate-200">作者</th>
                                  <th className="p-2.5 border border-slate-200">单价</th>
                                  <th className="p-2.5 border border-slate-200">数量</th>
                                  <th className="p-2.5 border border-slate-200">小计</th>
                                  <th className="p-2.5 border border-slate-200">操作</th>
                                </tr>
                              </thead>
                              <tbody>
                                {Object.entries(cart).map(([idStr, qty]) => {
                                  const b = BOOKS.find((item) => item.id === Number(idStr))!
                                  return (
                                    <tr key={b.id}>
                                      <td className="p-2 border border-slate-200">{b.id}</td>
                                      <td className="p-2 border border-slate-200">《{b.name}》</td>
                                      <td className="p-2 border border-slate-200">{b.author}</td>
                                      <td className="p-2 border border-slate-200">￥{b.price.toFixed(2)}</td>
                                      <td className="p-2 border border-slate-200">{qty}</td>
                                      <td className="p-2 border border-slate-200 text-[#c0392b] font-bold">
                                        ￥{(b.price * qty).toFixed(2)}
                                      </td>
                                      <td className="p-2 border border-slate-200">
                                        <button
                                          onClick={() => {
                                            const next = { ...cart }
                                            delete next[b.id]
                                            setCart(next)
                                          }}
                                          className="bg-slate-500 text-white text-xs px-2.5 py-1 rounded cursor-pointer"
                                        >
                                          移除
                                        </button>
                                      </td>
                                    </tr>
                                  )
                                })}
                                <tr className="bg-blue-50/70 font-bold">
                                  <td colSpan={4} className="p-2 border border-slate-200">
                                    合计
                                  </td>
                                  <td className="p-2 border border-slate-200">{totalCount} 本</td>
                                  <td className="p-2 border border-slate-200 text-[#c0392b]">
                                    ￥{totalPrice.toFixed(2)}
                                  </td>
                                  <td className="p-2 border border-slate-200"></td>
                                </tr>
                              </tbody>
                            </table>
                          </div>
                        )}
                        <div className="flex gap-2 mt-4">
                          <button
                            onClick={() => setDemoPage('bookList')}
                            className="bg-[#2e75b6] text-white text-xs px-3.5 py-2 rounded cursor-pointer"
                          >
                            继续购物
                          </button>
                          {totalCount > 0 && (
                            <button
                              onClick={() => setCart({})}
                              className="bg-slate-500 text-white text-xs px-3.5 py-2 rounded cursor-pointer"
                            >
                              清空购物车
                            </button>
                          )}
                          <button
                            onClick={handleDemoLogout}
                            className="bg-[#c0392b] text-white text-xs px-3.5 py-2 rounded cursor-pointer"
                          >
                            安全退出
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>

            {/* Console Output */}
            <div className="bg-slate-900 text-slate-200 rounded-xl p-4 font-mono text-xs space-y-1">
              <div className="text-slate-400 font-semibold mb-2">Tomcat Server 控制台输出（实时观察 sessionId）：</div>
              {logs.map((ln, idx) => (
                <div key={idx} className={ln.includes('[AddCartServlet]') ? 'text-emerald-400 font-semibold' : ''}>
                  {ln}
                </div>
              ))}
            </div>
          </div>
        )}
      </main>

      {/* Lightbox Modal */}
      {lightbox && (
        <div
          onClick={() => setLightbox(null)}
          className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center p-4"
        >
          <div
            onClick={(e) => e.stopPropagation()}
            className="bg-white rounded-xl max-w-5xl w-full max-h-[92vh] flex flex-col overflow-hidden shadow-2xl"
          >
            <div className="px-5 py-3.5 border-b border-slate-200 flex items-center justify-between">
              <div>
                <h3 className="font-bold text-slate-900">{lightbox.title}</h3>
                <p className="text-xs text-slate-500">{lightbox.detail}</p>
              </div>
              <div className="flex items-center gap-3">
                <a
                  href={lightbox.src}
                  download={lightbox.filename}
                  className="inline-flex items-center gap-1.5 bg-[#2e75b6] hover:bg-[#1f4e79] text-white text-xs font-semibold px-3 py-1.5 rounded-lg"
                >
                  <DownloadIcon />
                  <span>下载原图</span>
                </a>
                <button
                  onClick={() => setLightbox(null)}
                  className="text-slate-500 hover:text-slate-800 text-sm font-semibold px-2 py-1 cursor-pointer"
                >
                  关闭
                </button>
              </div>
            </div>
            <div className="p-4 bg-slate-100 overflow-auto flex-1 flex items-start justify-center">
              <img src={lightbox.src} alt={lightbox.title} className="max-w-full h-auto rounded border border-slate-300" />
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
