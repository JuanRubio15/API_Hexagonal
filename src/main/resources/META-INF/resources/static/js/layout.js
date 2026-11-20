/* =====================================================================
   Armazón común de las vistas internas: barra lateral, barra superior y
   (opcional) panel derecho. Cada página solo declara en <body>:
     data-page="facturas"  data-title="Facturas"  data-right="true|false"
   y pone su contenido dentro de <main id="contenido">.
   ===================================================================== */
(function(){
  /* Permisos por rol: espejo del enum Role del backend. Solo muestra u oculta; el que
     realmente bloquea es el servidor (403 con @RequiresPermission). */
  const ROLES = {
    ADMINISTRATOR: {label:'Administrador', can:['READ','CONVERT','MANAGE']},
    OPERATOR:      {label:'Operador',      can:['READ','CONVERT']},
    VIEWER:        {label:'Visualizador',  can:['READ']}
  };
  const roleKey = (sessionStorage.getItem('portal_role') || '').toUpperCase();
  const role = ROLES[roleKey] || {label:'Sin rol', can:[]};
  const can = p => role.can.includes(p);
  window.portalCan = can;

  const MENU = [
    {grupo:'Panel', items:[['dashboard','Resumen','home','dashboard.html','READ']]},
    {grupo:'Operación', items:[
      ['facturas','Facturas','doc','facturas.html','READ'],
      ['enviar','Enviar factura','send','enviar-factura.html','CONVERT'],
      ['syscafe','Consulta SysCafé','cloud','consulta-syscafe.html','READ']]},
    {grupo:'Catálogos', items:[
      ['clientes','Clientes','users','clientes.html','READ'],
      ['equivalencias','Equivalencias','swap','equivalencias.html','MANAGE']]},
    {grupo:'Cuenta', items:[['cuenta','Mi cuenta','user','cuenta.html','READ']]}
  ];
  const body = document.body;
  const page = body.dataset.page || '';
  const title = body.dataset.title || 'Punto y coma';
  const withRight = body.dataset.right === 'true';
  const parent = body.dataset.parent || 'Panel';
  const stage = document.querySelector('.stage');
  if(!stage) return;

  /* Si el rol no puede ver esta página, vuelve al resumen */
  const needed = {enviar:'CONVERT', equivalencias:'MANAGE'}[page] || 'READ';
  if (!can(needed)) {
    sessionStorage.setItem('portal_aviso', 'Su rol no tiene permiso para esa sección.');
    location.replace('dashboard.html');
    return;
  }
  const aviso = sessionStorage.getItem('portal_aviso');
  if (aviso) { sessionStorage.removeItem('portal_aviso'); setTimeout(() => window.toast && toast(aviso, 'bad'), 300); }

  /* Barra lateral */
  const nav = MENU.map(g => {
    const items = g.items.filter(it => can(it[4]));
    if (!items.length) return '';
    return `<div class="nav-label">${g.grupo}</div><nav class="nav" aria-label="${g.grupo}">` +
      items.map(([id,txt,ic,href]) =>
        `<a href="${href}" ${id===page||(id==='facturas'&&page==='factura-detalle')?'aria-current="page"':''}>${ICON(ic)}<span>${txt}</span></a>`).join('') + `</nav>`;
  }).join('');
  const side = document.createElement('aside');
  side.className = 'sidebar'; side.id = 'sidebar';
  side.innerHTML = `
    <a class="brand" href="dashboard.html" aria-label="Punto y coma · inicio">
      <img src="img/logo-dragon-morado.png" alt="" width="26" height="34">
      <span><b>Punto y coma</b><small>Sistema de facturación</small></span>
    </a>
    ${nav}
    <div class="side-user">
      <span class="avatar" id="side-avatar" aria-hidden="true"></span>
      <span><b id="side-user"></b><small id="side-role"></small></span>
      <button class="logout" id="btn-logout" title="Cerrar sesión" aria-label="Cerrar sesión">${ICON('logout')}</button>
    </div>`;
  stage.parentElement.insertBefore(side, stage);

  const uname = sessionStorage.getItem('portal_user') || 'Usuario';
  document.getElementById('side-user').textContent = uname;
  document.getElementById('side-role').textContent = role.label;
  document.getElementById('side-avatar').textContent = uname.slice(0, 2).toUpperCase();
  /* Barra superior */
  const top = document.createElement('header');
  top.className = 'topbar';
  top.innerHTML = `
    <button class="icon-btn only-mobile" id="btn-menu" aria-label="Abrir menú">${ICON('menu')}</button>
    <button class="icon-btn" data-pendiente aria-label="Favorito">${ICON('star')}</button>
    <div class="crumb"><span>${parent}</span><span>/</span><b>${title}</b></div>
    <span class="spacer"></span>
    <label class="search">${ICON('search')}<input type="search" placeholder="Buscar" aria-label="Buscar"><kbd>⌘K</kbd></label>
    <button class="icon-btn" data-pendiente aria-label="Tema">${ICON('sun')}</button>
    <button class="icon-btn" data-pendiente aria-label="Historial">${ICON('clock')}</button>
    <button class="icon-btn" ${withRight?'id="btn-right"':'data-pendiente'} aria-label="Notificaciones">${ICON('bell')}</button>`;
  stage.insertBefore(top, stage.firstChild);

  /* Panel derecho (solo donde la página lo pide) */
  if (withRight) {
    const right = document.createElement('aside');
    right.className = 'rightbar';
    right.id = 'rightbar';
    right.innerHTML = '<section><h4>Últimos documentos</h4><ul class="feed" id="recent-documents"></ul></section>';
    stage.parentElement.appendChild(right);
  }

  /* Comportamiento del armazón */
  const scrim = document.createElement('div'); scrim.className = 'scrim'; scrim.hidden = true; document.body.appendChild(scrim);
  const closeAll = () => { side.classList.remove('open'); const r=document.getElementById('rightbar'); r&&r.classList.remove('open'); scrim.hidden = true; };
  document.getElementById('btn-menu').addEventListener('click', () => { side.classList.add('open'); scrim.hidden = false; });
  const br = document.getElementById('btn-right');
  if(br) br.addEventListener('click', () => { const r=document.getElementById('rightbar'); const o=r.classList.toggle('open'); scrim.hidden = !o; });
  scrim.addEventListener('click', closeAll);
  document.addEventListener('keydown', e => { if(e.key==='Escape') closeAll(); });

  /* Cerrar sesión: borra el token que guardó el login (misma clave que panel.html) */
  document.getElementById('btn-logout').addEventListener('click', () => {
    portal.logout();
  });
})();

/* ---------- Utilidades compartidas ---------- */
window.$  = (s, r=document) => r.querySelector(s);
window.$$ = (s, r=document) => [...r.querySelectorAll(s)];
window.esc = v => String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
window.cop = n => new Intl.NumberFormat('es-CO',{style:'currency',currency:'COP',maximumFractionDigits:0}).format(n||0);
window.fechaCorta = iso => { if (!iso) return '—'; const [y,m,d] = iso.split('-'); return `${d}/${m}/${y}`; };
window.toast = (msg, tipo='') => {
  let box = document.getElementById('toasts');
  if(!box){ box = document.createElement('div'); box.id='toasts'; box.setAttribute('role','status'); box.setAttribute('aria-live','polite'); document.body.appendChild(box); }
  const el = document.createElement('div'); el.className = 'toast '+tipo; el.textContent = msg; box.appendChild(el);
  setTimeout(()=>el.remove(), 3800);
};
/* Estados de factura -> clase visual (mismos colores en toda la app) */
window.estadoTag = e => ({validada:['ok','Convertida'],enviada:['info','Enviada a SysCafé'],pendiente:['warn','Pendiente'],rechazada:['bad','Rechazada']}[e]||['','—']);
window.tagHTML = e => { const [c,t] = estadoTag(e); return `<span class="tag ${c}">${t}</span>`; };

/* Botones aún sin función: avisan en lugar de simular una acción */
document.addEventListener('click', e => {
  const b = e.target.closest('[data-pendiente]');
  if(b){ e.preventDefault(); toast('Esta acción se conectará en la siguiente etapa.'); }
  const o = e.target.closest('[data-abrir]');
  if(o){ const d = document.getElementById(o.dataset.abrir); d && d.showModal(); }
  const c = e.target.closest('[data-cerrar]');
  if(c){ c.closest('dialog').close(); }
});
document.addEventListener('click', e => { if(e.target.tagName==='DIALOG') e.target.close(); });
