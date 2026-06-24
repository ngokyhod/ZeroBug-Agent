(function(){const n=document.createElement("link").relList;if(n&&n.supports&&n.supports("modulepreload"))return;for(const a of document.querySelectorAll('link[rel="modulepreload"]'))t(a);new MutationObserver(a=>{for(const r of a)if(r.type==="childList")for(const i of r.addedNodes)i.tagName==="LINK"&&i.rel==="modulepreload"&&t(i)}).observe(document,{childList:!0,subtree:!0});function s(a){const r={};return a.integrity&&(r.integrity=a.integrity),a.referrerPolicy&&(r.referrerPolicy=a.referrerPolicy),a.crossOrigin==="use-credentials"?r.credentials="include":a.crossOrigin==="anonymous"?r.credentials="omit":r.credentials="same-origin",r}function t(a){if(a.ep)return;a.ep=!0;const r=s(a);fetch(a.href,r)}})();const m={backendConnected:!1,awsEnabled:!1,profile:"offline",storageType:"local",listeners:new Set};function S(e){Object.assign(m,e),m.listeners.forEach(n=>n(m))}function D(e){return m.listeners.add(e),()=>m.listeners.delete(e)}function h(){return m.backendConnected}function $(){return m.backendConnected&&m.awsEnabled}const q={id:0,email:"demo@gmail.com",fullName:"Demo User (Offline)",role:"USER"},v=[{id:1,name:"Demo Java Project",sourceType:"GIT",gitUrl:"https://github.com/example/demo.git",createdAt:"2026-01-15T10:00:00"}],H=[{id:1,requirements:"Viết JUnit test cho OrderService — thanh toán thành công",createdAt:"2026-01-14T15:30:00",projectId:1,projectName:"Demo Java Project"}],x=[{name:"src",path:"src",type:"folder",children:[{name:"main",path:"src/main",type:"folder",children:[{name:"java",path:"src/main/java",type:"folder",children:[{name:"OrderService.java",path:"src/main/java/OrderService.java",type:"file",language:"java"}]}]}]}],_=`public class OrderService {
    public boolean pay(double amount, double balance) {
        if (amount > balance) return false;
        return true;
    }
}`,R=`@Test
void pay_success_whenBalanceEnough() {
    OrderService service = new OrderService();
    assertTrue(service.pay(100, 500));
}

@Test
void pay_fail_whenBalanceInsufficient() {
    OrderService service = new OrderService();
    assertFalse(service.pay(500, 100));
}`,U={allOk:!1,services:[{service:"Backend",status:"DISABLED",message:"Chưa kết nối server — đang xem giao diện demo."},{service:"RDS PostgreSQL",status:"DISABLED",message:"Cần kết nối backend cloud."},{service:"Amazon S3",status:"DISABLED",message:"Cần kết nối backend cloud."},{service:"Amazon Bedrock",status:"DISABLED",message:"Cần kết nối backend cloud."}]};function z(e,n={}){const s=(n.method||"GET").toUpperCase();if(e==="/api/health")return{ok:!0,status:200,json:async()=>({status:"offline"})};if(e==="/api/auth/login"&&s==="POST")return{ok:!0,status:200,json:async()=>({success:!0,user:q})};if(e==="/api/auth/me"){const t=localStorage.getItem("zerobug_mock_user");return t?{ok:!0,status:200,json:async()=>JSON.parse(t)}:{ok:!1,status:401,json:async()=>({error:"Chưa đăng nhập"})}}return e==="/api/auth/logout"?(localStorage.removeItem("zerobug_mock_user"),{ok:!0,status:200,json:async()=>({success:!0})}):e==="/api/auth/register"?{ok:!0,status:200,json:async()=>({success:!0,message:"Demo: đăng ký thành công (offline)"})}:e==="/api/auth/forgot-password"||e==="/api/auth/reset-password"?{ok:!0,status:200,json:async()=>({success:!0,message:"Demo offline — chức năng mô phỏng."})}:e==="/api/projects/import/git"&&s==="POST"?{ok:!0,status:200,json:async()=>({success:!0,message:"Demo: Import Git thành công (offline)",project:{...v[0],id:Date.now()%1e5}})}:e==="/api/projects/import/zip"&&s==="POST"?{ok:!0,status:200,json:async()=>({success:!0,message:"Demo: Upload Zip thành công (offline)",project:{...v[0],id:Date.now()%1e5,sourceType:"ZIP"}})}:s==="DELETE"&&e.match(/^\/api\/projects\/\d+$/)?{ok:!0,status:200,json:async()=>({success:!0,message:"Demo: đã xóa (offline)"})}:e==="/api/projects"?{ok:!0,status:200,json:async()=>v}:e.match(/^\/api\/projects\/\d+$/)?{ok:!0,status:200,json:async()=>v[0]}:e.match(/^\/api\/projects\/\d+\/files$/)?{ok:!0,status:200,json:async()=>x}:e.startsWith("/api/projects/")&&e.includes("/file?")?{ok:!0,status:200,json:async()=>({path:"src/main/java/OrderService.java",content:_})}:e.match(/^\/api\/projects\/\d+\/generate$/)&&s==="POST"?{ok:!0,status:200,json:async()=>({success:!0,response:R,aiSource:"mock",awsMessage:"Chế độ demo offline — kết nối backend để sinh test thật."})}:e.startsWith("/api/generations/recent")?{ok:!0,status:200,json:async()=>H}:e==="/api/aws/status"?{ok:!0,status:200,json:async()=>U}:{ok:!1,status:503,json:async()=>({error:"Chế độ offline — chức năng chưa khả dụng"})}}const O="";let k=!1;async function A(){try{const e=await fetch(`${O}/api/health`,{credentials:"include"});if(e.ok){const n=await e.json();return k=!1,S({backendConnected:!0,awsEnabled:n.awsEnabled===!0,profile:n.profile||"default",storageType:n.storageType||"local"}),n}}catch{}return k=!0,S({backendConnected:!1,awsEnabled:!1,profile:"offline",storageType:"local"}),null}async function l(e,n={}){if(k){if(e==="/api/auth/login"&&n.method==="POST"){const t=JSON.parse(n.body||"{}");localStorage.setItem("zerobug_mock_user",JSON.stringify({id:0,email:t.email||"demo@gmail.com",fullName:"Demo User (Offline)",role:"USER"}))}return z(e,n)}const s={...n.headers||{}};return n.body&&!(n.body instanceof FormData)&&!s["Content-Type"]&&(s["Content-Type"]="application/json"),fetch(`${O}${e}`,{...n,headers:s,credentials:"include"})}function K(){const e=document.getElementById("connection-banner");if(!e)return;const n=s=>{var t;if(s.backendConnected){const a=s.awsEnabled?"AWS đã kết nối — đầy đủ chức năng.":"Backend local — AI dùng mock (AWS chưa bật).";e.className="connection-banner connected",e.innerHTML=`
        <span>✓ Đã kết nối backend (${s.profile}) — ${a}</span>
        <button type="button" class="banner-btn" id="recheck-btn">Kiểm tra lại</button>
      `}else e.className="connection-banner offline",e.innerHTML=`
        <span>⚠ Chế độ offline — xem giao diện demo. Kết nối backend để dùng đăng ký, import, sinh test thật.</span>
        <button type="button" class="banner-btn" id="recheck-btn">Thử kết nối</button>
      `;(t=e.querySelector("#recheck-btn"))==null||t.addEventListener("click",async()=>{e.innerHTML="<span>Đang kiểm tra kết nối...</span>",await A()})};D(n),n(m)}async function L(e){e.innerHTML=`
    <div class="auth-body">
      <div class="auth-container">
        <div class="auth-card">
          <div class="auth-header">
            <h1>ZeroBug Agent</h1>
            <p>Đăng nhập để sử dụng ứng dụng</p>
          </div>
          <div id="login-alert"></div>
          <form id="login-form" class="auth-form">
            <div class="form-group">
              <label for="email">Email Gmail</label>
              <input type="email" id="email" name="email" placeholder="you@gmail.com" required>
            </div>
            <div class="form-group">
              <label for="password">Mật khẩu</label>
              <input type="password" id="password" name="password" placeholder="••••••••" required>
            </div>
            <button type="submit" class="btn btn-primary btn-block">Đăng nhập</button>
          </form>
          <div class="auth-links">
            <a href="#/forgot-password">Quên mật khẩu?</a>
            <a href="#/register">Đăng ký tài khoản</a>
          </div>
          <div class="admin-hint">
            <strong>Tài khoản Admin test:</strong><br>
            Email: <code>admin@gmail.com</code><br>
            Mật khẩu: <code>Admin@123456</code>
            ${h()?"":"<br><em>Offline: mọi email/mật khẩu đều vào được demo.</em>"}
          </div>
        </div>
      </div>
    </div>`,document.getElementById("login-form").addEventListener("submit",async n=>{n.preventDefault();const s=document.getElementById("email").value.trim(),t=document.getElementById("password").value,a=document.getElementById("login-alert"),r=await l("/api/auth/login",{method:"POST",body:JSON.stringify({email:s,password:t})}),i=await r.json();if(r.ok&&i.success){window.location.hash="#/home";return}a.innerHTML=`<div class="alert alert-error">${i.error||"Đăng nhập thất bại"}</div>`})}function I(e){return e?new Date(e).toLocaleString("vi-VN",{day:"2-digit",month:"2-digit",year:"numeric",hour:"2-digit",minute:"2-digit"}):""}function g(e,n={}){const s=e?`<span class="nav-user">${e.fullName} (${e.email})</span>
       ${e.role==="ADMIN"?'<span class="badge">ADMIN</span>':""}
       <button type="button" class="btn btn-secondary btn-sm" id="logout-btn">Đăng xuất</button>`:"";return`
    <nav class="navbar">
      <div class="nav-brand"><a href="#/home">ZeroBug Agent</a></div>
      <div class="nav-links">
        ${n.showHome?'<a href="#/home">Trang chủ</a>':""}
        ${s}
      </div>
    </nav>`}async function F(e){e.innerHTML=`
    ${g(null)}
    <main class="container narrow">
      <div class="auth-card" style="margin-top:2rem;">
        <div class="auth-header">
          <h1>Đăng ký</h1>
          <p>Tạo tài khoản Gmail mới</p>
        </div>
        ${h()?"":'<div class="alert alert-error">Chế độ offline — đăng ký chỉ mô phỏng. Kết nối backend để đăng ký thật.</div>'}
        <div id="register-alert"></div>
        <form id="register-form" class="auth-form">
          <div class="form-group">
            <label for="fullName">Họ tên</label>
            <input type="text" id="fullName" required minlength="2">
          </div>
          <div class="form-group">
            <label for="email">Email Gmail</label>
            <input type="email" id="email" placeholder="you@gmail.com" required>
          </div>
          <div class="form-group">
            <label for="password">Mật khẩu</label>
            <input type="password" id="password" required minlength="8">
          </div>
          <div class="form-group">
            <label for="confirmPassword">Xác nhận mật khẩu</label>
            <input type="password" id="confirmPassword" required minlength="8">
          </div>
          <button type="submit" class="btn btn-primary btn-block">Đăng ký</button>
        </form>
        <div class="auth-links" style="justify-content:center;margin-top:1rem;">
          <a href="#/login">Đã có tài khoản? Đăng nhập</a>
        </div>
      </div>
    </main>`,document.getElementById("register-form").addEventListener("submit",async n=>{n.preventDefault();const s={fullName:document.getElementById("fullName").value.trim(),email:document.getElementById("email").value.trim(),password:document.getElementById("password").value,confirmPassword:document.getElementById("confirmPassword").value},t=await l("/api/auth/register",{method:"POST",body:JSON.stringify(s)}),a=await t.json(),r=document.getElementById("register-alert");if(t.ok&&a.success){r.innerHTML=`<div class="alert alert-success">${a.message}</div>`,setTimeout(()=>{window.location.hash="#/login"},1500);return}r.innerHTML=`<div class="alert alert-error">${a.error||"Đăng ký thất bại"}</div>`})}async function G(e){e.innerHTML=`
    ${g(null)}
    <main class="container narrow">
      <div class="auth-card" style="margin-top:2rem;">
        <div class="auth-header">
          <h1>Quên mật khẩu</h1>
          <p>Nhập email để nhận link đặt lại mật khẩu</p>
        </div>
        <div id="forgot-alert"></div>
        <form id="forgot-form" class="auth-form">
          <div class="form-group">
            <label for="email">Email</label>
            <input type="email" id="email" required>
          </div>
          <button type="submit" class="btn btn-primary btn-block">Gửi link reset</button>
        </form>
        <div class="auth-links" style="justify-content:center;margin-top:1rem;">
          <a href="#/login">← Quay lại đăng nhập</a>
        </div>
      </div>
    </main>`,document.getElementById("forgot-form").addEventListener("submit",async n=>{n.preventDefault();const s=document.getElementById("email").value.trim(),a=await(await l("/api/auth/forgot-password",{method:"POST",body:JSON.stringify({email:s})})).json();document.getElementById("forgot-alert").innerHTML=`<div class="alert alert-success">${a.message||a.error}</div>`})}async function J(e,n){const s=n.get("token")||"";e.innerHTML=`
    ${g(null)}
    <main class="container narrow">
      <div class="auth-card" style="margin-top:2rem;">
        <div class="auth-header">
          <h1>Đặt lại mật khẩu</h1>
        </div>
        <div id="reset-alert"></div>
        <form id="reset-form" class="auth-form">
          <input type="hidden" id="token" value="${s}">
          <div class="form-group">
            <label for="password">Mật khẩu mới</label>
            <input type="password" id="password" required minlength="8">
          </div>
          <div class="form-group">
            <label for="confirmPassword">Xác nhận mật khẩu</label>
            <input type="password" id="confirmPassword" required minlength="8">
          </div>
          <button type="submit" class="btn btn-primary btn-block">Đặt lại mật khẩu</button>
        </form>
      </div>
    </main>`,document.getElementById("reset-form").addEventListener("submit",async t=>{t.preventDefault();const a={token:document.getElementById("token").value,password:document.getElementById("password").value,confirmPassword:document.getElementById("confirmPassword").value},r=await l("/api/auth/reset-password",{method:"POST",body:JSON.stringify(a)}),i=await r.json(),o=document.getElementById("reset-alert");if(r.ok&&i.success){o.innerHTML=`<div class="alert alert-success">${i.message}</div>`,setTimeout(()=>{window.location.hash="#/login"},1500);return}o.innerHTML=`<div class="alert alert-error">${i.error||"Thất bại"}</div>`})}const W="modulepreload",V=function(e){return"/"+e},B={},Z=function(n,s,t){let a=Promise.resolve();if(s&&s.length>0){document.getElementsByTagName("link");const i=document.querySelector("meta[property=csp-nonce]"),o=(i==null?void 0:i.nonce)||(i==null?void 0:i.getAttribute("nonce"));a=Promise.allSettled(s.map(c=>{if(c=V(c),c in B)return;B[c]=!0;const u=c.endsWith(".css"),p=u?'[rel="stylesheet"]':"";if(document.querySelector(`link[href="${c}"]${p}`))return;const d=document.createElement("link");if(d.rel=u?"stylesheet":W,u||(d.as="script"),d.crossOrigin="",d.href=c,o&&d.setAttribute("nonce",o),document.head.appendChild(d),u)return new Promise((w,N)=>{d.addEventListener("load",w),d.addEventListener("error",()=>N(new Error(`Unable to preload CSS for ${c}`)))})}))}function r(i){const o=new Event("vite:preloadError",{cancelable:!0});if(o.payload=i,window.dispatchEvent(o),!o.defaultPrevented)throw i}return a.then(i=>{for(const o of i||[])o.status==="rejected"&&r(o.reason);return n().catch(r)})};function E(e){const n=document.createElement("div");return n.textContent=e,n.innerHTML}async function T(e,n={}){const s=document.getElementById(e);if(s){s.innerHTML='<div class="aws-status-loading">Đang kiểm tra dịch vụ AWS...</div>';try{const t=await l("/api/aws/status");if(!t.ok)throw new Error("HTTP "+t.status);const a=await t.json();X(s,a,n)}catch(t){s.innerHTML=`<div class="aws-status-error">Không thể kiểm tra trạng thái AWS: ${E(t.message)}</div>`}}}function X(e,n,s){var o;const t=s.compact===!0,a=(n.services||[]).map(c=>{const u=c.status==="OK"?"aws-ok":c.status==="DISABLED"?"aws-disabled":"aws-error",p=c.status==="OK"?"✓":c.status==="DISABLED"?"–":"✗";return`
      <div class="aws-status-item ${u}">
        <div class="aws-status-head">
          <span class="aws-status-icon">${p}</span>
          <strong>${E(c.service)}</strong>
          <span class="aws-status-badge">${c.status}</span>
        </div>
        <div class="aws-status-msg">${E(c.message)}</div>
      </div>`}).join(""),r=n.allOk?"aws-summary-ok":"aws-summary-warn",i=n.allOk?"Tất cả dịch vụ AWS đang hoạt động bình thường.":"Một hoặc nhiều dịch vụ AWS chưa sẵn sàng — xem chi tiết bên dưới.";e.innerHTML=`
    <div class="aws-status-panel ${t?"compact":""}">
      <div class="aws-summary ${r}">${i}</div>
      <div class="aws-status-list">${a}</div>
      ${s.showRefresh?'<button type="button" class="btn btn-secondary btn-sm aws-refresh-btn">Kiểm tra lại</button>':""}
    </div>`,s.showRefresh&&((o=e.querySelector(".aws-refresh-btn"))==null||o.addEventListener("click",()=>{T(e.id,s)}))}function b(e,n,s){const t=document.getElementById(e);t&&(t.className=`aws-banner aws-banner-${s}`,t.textContent=n,t.classList.remove("hidden"))}async function Q(e,n){var i;const[s,t]=await Promise.all([l("/api/projects"),l("/api/generations/recent?limit=5")]),a=s.ok?await s.json():[],r=t.ok?await t.json():[];e.innerHTML=`
    ${g(n)}
    <main class="container">
      <section class="hero">
        <h1>Xin chào, <span>${n.fullName}</span>!</h1>
        <p>Import source code từ Git hoặc file Zip, sau đó mở IDE để sinh Unit Test bằng AI Agent.</p>
        <a href="#/workspace/new" class="btn btn-primary">+ Tạo dự án mới</a>
        ${h()?"":'<p class="meta" style="margin-top:0.75rem;color:#92400e;">Demo offline — dữ liệu mẫu. Kết nối backend để làm việc thật.</p>'}
      </section>

      <section class="grid-2">
        <div class="card">
          <h2>Dự án của bạn</h2>
          ${a.length===0?'<div class="empty-state">Chưa có dự án nào. Hãy tạo dự án mới để bắt đầu.</div>':`<ul class="project-list">${a.map(o=>`
                <li class="project-item">
                  <div>
                    <strong>${o.name}</strong>
                    <span class="meta">${o.sourceType} · ${I(o.createdAt)}</span>
                  </div>
                  <div class="project-actions">
                    <a href="#/workspace/${o.id}" class="btn btn-primary btn-sm">Mở IDE</a>
                    <button type="button" class="btn btn-danger btn-sm delete-btn" data-id="${o.id}">Xóa</button>
                  </div>
                </li>`).join("")}</ul>`}
        </div>

        <div class="card">
          <h2>Lịch sử sinh test gần đây</h2>
          ${r.length===0?'<div class="empty-state">Chưa có lịch sử.</div>':`<ul class="history-list">${r.map(o=>{var c;return`
                <li>
                  <div class="history-req">${((c=o.requirements)==null?void 0:c.length)>80?o.requirements.slice(0,80)+"…":o.requirements}</div>
                  <div class="meta">${I(o.createdAt)}</div>
                </li>`}).join("")}</ul>`}
        </div>

        <div class="card aws-status-card">
          <h2>Trạng thái dịch vụ AWS</h2>
          <div id="awsStatusPanel"></div>
        </div>
      </section>
    </main>`,(i=document.getElementById("logout-btn"))==null||i.addEventListener("click",async()=>{await l("/api/auth/logout",{method:"POST"}),window.location.hash="#/login"}),e.querySelectorAll(".delete-btn").forEach(o=>{o.addEventListener("click",async()=>{if(!confirm("Xóa dự án này?"))return;const c=o.dataset.id,u=await l(`/api/projects/${c}`,{method:"DELETE"}),p=await u.json();if(u.ok&&p.success){const{navigate:d}=await Z(async()=>{const{navigate:w}=await Promise.resolve().then(()=>ce);return{navigate:w}},void 0);await d();return}else alert(p.error||"Xóa thất bại")})}),T("awsStatusPanel",{showRefresh:!0})}async function Y(e,n){e.innerHTML=`
    ${g(n,{showHome:!0})}
    <main class="container narrow">
      <h1 style="margin:1.5rem 0 1rem;color:#1e293b;">Tạo dự án mới</h1>
      ${h()?"":'<div class="alert alert-error">Chế độ offline — import chỉ mô phỏng thành công. Kết nối backend để import Git/Zip thật.</div>'}
      <div id="import-alert"></div>

      <div class="tabs">
        <button class="tab-btn active" data-tab="git">Git Repository</button>
        <button class="tab-btn" data-tab="zip">Upload Zip</button>
      </div>

      <div id="tab-git" class="tab-panel active card">
        <form id="git-form" class="auth-form">
          <div class="form-group">
            <label for="gitUrl">Git URL (public)</label>
            <input type="url" id="gitUrl" placeholder="https://github.com/user/repo.git" required>
          </div>
          <div class="form-group">
            <label for="gitProjectName">Tên dự án (tùy chọn)</label>
            <input type="text" id="gitProjectName" placeholder="My Java Project">
          </div>
          <button type="submit" class="btn btn-primary">Import từ Git</button>
        </form>
      </div>

      <div id="tab-zip" class="tab-panel card">
        <form id="zip-form" class="auth-form">
          <div class="form-group">
            <label for="zipFile">File Source Code (.zip)</label>
            <input type="file" id="zipFile" accept=".zip" required>
          </div>
          <div class="form-group">
            <label for="zipProjectName">Tên dự án (tùy chọn)</label>
            <input type="text" id="zipProjectName" placeholder="My Java Project">
          </div>
          <button type="submit" class="btn btn-primary">Upload Zip</button>
        </form>
      </div>
    </main>`,document.querySelectorAll(".tab-btn").forEach(t=>{t.addEventListener("click",()=>{document.querySelectorAll(".tab-btn").forEach(a=>a.classList.remove("active")),document.querySelectorAll(".tab-panel").forEach(a=>a.classList.remove("active")),t.classList.add("active"),document.getElementById(`tab-${t.dataset.tab}`).classList.add("active")})}),document.getElementById("git-form").addEventListener("submit",async t=>{t.preventDefault();const a=document.getElementById("gitUrl").value.trim(),r=document.getElementById("gitProjectName").value.trim(),i=await l("/api/projects/import/git",{method:"POST",body:JSON.stringify({gitUrl:a,projectName:r||null})}),o=await i.json();s(o,i.ok)}),document.getElementById("zip-form").addEventListener("submit",async t=>{t.preventDefault();const a=document.getElementById("zipFile"),r=new FormData;r.append("zipFile",a.files[0]);const i=document.getElementById("zipProjectName").value.trim();i&&r.append("projectName",i);const o=await l("/api/projects/import/zip",{method:"POST",body:r}),c=await o.json();s(c,o.ok)});function s(t,a){const r=document.getElementById("import-alert");if(a&&t.success){window.location.hash=`#/workspace/${t.project.id}`;return}r.innerHTML=`<div class="alert alert-error">${t.error||"Import thất bại"}</div>`}}let f=null,y=null;async function ee(e,n,s){y=s;const t=await l(`/api/projects/${s}`);if(!t.ok){e.innerHTML='<div class="container"><div class="alert alert-error">Không tìm thấy dự án.</div></div>';return}const a=await t.json();e.innerHTML=`
    <header class="ide-header">
      <div class="ide-header-left">
        <a href="#/home" class="btn btn-secondary btn-sm">← Trang chủ</a>
        <span class="ide-title">${a.name}</span>
        <span class="badge">${a.sourceType}</span>
      </div>
      <div class="ide-header-right">
        <span>${n.email}</span>
      </div>
    </header>

    <div class="ide-layout">
      <aside class="ide-sidebar">
        <div class="sidebar-header">Explorer</div>
        <div id="fileTree" class="file-tree"></div>
      </aside>

      <section class="ide-editor-panel">
        <div class="editor-tabs">
          <span id="activeFileTab" class="editor-tab active">Chọn file để xem</span>
        </div>
        <div id="monacoEditor" class="monaco-container"></div>
      </section>

      <aside class="ide-agent-panel">
        <div class="agent-header">
          <h3>AI Test Agent</h3>
          <p>Mô tả yêu cầu kiểm thử</p>
        </div>
        <div id="generateAwsBanner" class="aws-banner hidden"></div>
        <div id="ideAwsStatus" class="aws-status-compact-wrap"></div>
        ${h()?$()?"":'<div class="alert alert-success" style="margin-bottom:0.75rem;font-size:0.8rem;background:#fef3c7;color:#92400e;">AWS chưa bật — AI dùng mock response.</div>':'<div class="alert alert-error" style="margin-bottom:0.75rem;font-size:0.8rem;">Offline demo — Generate trả kết quả mẫu.</div>'}
        <textarea id="requirementsInput" rows="6" placeholder="Ví dụ: Viết JUnit test cho OrderService..."></textarea>
        <button id="generateBtn" class="btn btn-primary btn-block">Generate Test</button>
        <div id="loadingIndicator" class="loading hidden">
          <div class="spinner"></div>
          <span>Agent đang phân tích và sinh test...</span>
        </div>
        <div class="agent-response">
          <div class="agent-response-header">
            <strong>Kết quả Agent</strong>
            <button id="copyBtn" class="btn btn-secondary btn-sm">Copy</button>
          </div>
          <pre id="agentOutput" class="agent-output">// Kết quả sẽ hiển thị ở đây...</pre>
        </div>
      </aside>
    </div>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.45.0/min/vs/editor/editor.main.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.45.0/min/vs/loader.min.js"><\/script>`,te(),T("ideAwsStatus",{compact:!0}),document.getElementById("generateBtn").addEventListener("click",se),document.getElementById("copyBtn").addEventListener("click",()=>{navigator.clipboard.writeText(document.getElementById("agentOutput").textContent).then(()=>alert("Đã copy!"))})}function te(){if(typeof require>"u"){document.getElementById("fileTree").innerHTML='<div class="tree-empty">Monaco Editor chưa tải.</div>';return}require.config({paths:{vs:"https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.45.0/min/vs"}}),require(["vs/editor/editor.main"],()=>{f=monaco.editor.create(document.getElementById("monacoEditor"),{value:`// Chọn file từ Explorer bên trái để xem source code
`,language:"java",theme:"vs-dark",automaticLayout:!0,readOnly:!0,minimap:{enabled:!0},fontSize:13,scrollBeyondLastLine:!1}),ne()})}async function ne(){const e=document.getElementById("fileTree");e.innerHTML='<div class="tree-loading">Đang tải cây thư mục...</div>';try{const n=await l(`/api/projects/${y}/files`);if(!n.ok)throw new Error("HTTP "+n.status);const s=await n.json();if(e.innerHTML="",!(s!=null&&s.length)){e.innerHTML='<div class="tree-empty">Không có file nào.</div>';return}P(s,e)}catch(n){e.innerHTML=`<div class="tree-empty">Không thể tải file: ${n.message}</div>`}}function P(e,n,s=0){e.forEach(t=>{var r;if(t.ignored)return;const a=document.createElement("div");if(a.className=`tree-item ${t.type==="folder"?"folder":"file"}`,a.style.paddingLeft=`${s*12+8}px`,a.innerHTML=`<span>${t.type==="folder"?"📁":"📄"}</span><span>${t.name}</span>`,t.type==="file"?a.addEventListener("click",i=>{i.stopPropagation(),ae(t.path,t.language||"plaintext"),document.querySelectorAll(".tree-item").forEach(o=>o.classList.remove("active")),a.classList.add("active")}):a.addEventListener("click",i=>{i.stopPropagation();const o=a.nextElementSibling;o!=null&&o.classList.contains("tree-children")&&o.classList.toggle("collapsed")}),n.appendChild(a),t.type==="folder"&&((r=t.children)!=null&&r.length)){const i=document.createElement("div");i.className="tree-children",n.appendChild(i),P(t.children,i,s+1)}})}async function ae(e,n){document.getElementById("activeFileTab").textContent=e;try{const t=await(await l(`/api/projects/${y}/file?path=${encodeURIComponent(e)}`)).json();if(t.error){f.setValue("// Lỗi: "+t.error);return}monaco.editor.setModelLanguage(f.getModel(),n),f.setValue(t.content)}catch(s){f.setValue("// Không thể tải file: "+s.message)}}async function se(){const e=document.getElementById("requirementsInput").value.trim();if(!e){alert("Vui lòng nhập yêu cầu kiểm thử!");return}const n=document.getElementById("generateBtn"),s=document.getElementById("loadingIndicator"),t=document.getElementById("agentOutput");n.disabled=!0,s.classList.remove("hidden"),t.textContent="// Agent đang xử lý...",b("generateAwsBanner",$()?"Đang gọi Amazon Bedrock...":"Đang xử lý (mock/local)...","info");try{const r=await(await l(`/api/projects/${y}/generate`,{method:"POST",body:JSON.stringify({requirements:e})})).json();if(r.error||r.success===!1){const i=r.awsMessage||r.error||"Lỗi không xác định";t.textContent="// Lỗi: "+i,b("generateAwsBanner",i,"error");return}t.textContent=r.response,b("generateAwsBanner",r.awsMessage||"Sinh test thành công.",r.aiSource==="bedrock"?"success":"info")}catch(a){t.textContent="// Lỗi kết nối: "+a.message,b("generateAwsBanner","Lỗi kết nối: "+a.message,"error")}finally{n.disabled=!1,s.classList.add("hidden")}}const re=["login","register","forgot-password","reset-password"];function ie(){const e=window.location.hash.slice(1)||"/home",[n,s]=e.split("?"),t=n.replace(/^\//,"").split("/").filter(Boolean),a=new URLSearchParams(s||"");return{segments:t,params:a}}async function oe(){const e=await l("/api/auth/me");return e.ok?e.json():null}async function j(){const e=document.getElementById("app"),{segments:n,params:s}=ie(),t=n[0]||"home";if(re.includes(t))return document.body.className="",t==="login"?L(e):t==="register"?F(e):t==="forgot-password"?G(e):t==="reset-password"?J(e,s):void 0;const a=await oe();if(!a)return window.location.hash="#/login",L(e);if(t==="home")return document.body.className="",Q(e,a);if(t==="workspace"){if(n[1]==="new")return document.body.className="",Y(e,a);if(n[1])return document.body.className="ide-body",ee(e,a,n[1])}window.location.hash="#/home"}function C(){window.addEventListener("hashchange",()=>j())}async function M(){await A(),C(),await j()}const ce=Object.freeze(Object.defineProperty({__proto__:null,initRouter:C,navigate:j,startApp:M},Symbol.toStringTag,{value:"Module"}));K();M();
