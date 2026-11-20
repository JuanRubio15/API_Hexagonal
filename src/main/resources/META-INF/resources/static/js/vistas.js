(async function(){
  const D = {}, page = document.body.dataset.page;

  const statCard = s => `<article class="stat ${s.tono}">
      <span class="lbl">${esc(s.etiqueta)}</span>
      <div class="val"><b class="num">${esc(s.valor)}</b><span class="delta">${esc(s.delta||'')} ${s.delta?ICON(s.sube?'up':'down'):''}</span></div>
    </article>`;
  const renderStats = (el, list) => { el.innerHTML = list.map(statCard).join(''); };

  const filaFactura = (f, completa) => completa
    ? `<tr class="click" data-id="${f.document_id}">
        <td><span class="cell-main">${esc(f.numero)}</span></td><td>${esc(f.noext)}</td>
        <td>${esc(f.cliente)}<span class="cell-sub">ID ${esc(f.nit)}</span></td>
        <td class="num">${fechaCorta(f.fecha)}</td>
        <td><span class="tag brand plain">${esc(f.tipo)}</span></td>
        <td class="r num">${cop(f.total)}</td><td>${tagHTML(f.estado)}</td>
        <td class="actions">${portalCan('MANAGE') ? `<a class="icon-btn" href="factura-detalle.html?id=${f.document_id}&amp;editar=1" aria-label="Editar">${ICON('edit')}</a>
        <button class="icon-btn" type="button" data-eliminar-factura aria-label="Eliminar">${ICON('trash')}</button>` : ''}</td></tr>`
    : `<tr class="click" data-id="${f.document_id}">
        <td><span class="cell-main">${esc(f.numero)}</span></td><td>${esc(f.cliente)}</td>
        <td class="num">${fechaCorta(f.fecha)}</td><td>${esc(f.sede||f.bodega||'')}</td>
        <td class="r num">${cop(f.total)}</td><td>${tagHTML(f.estado)}</td></tr>`;
  const irDetalle = tbody => tbody.addEventListener('click', e => {
    if(e.target.closest('button, a')) return;
    const tr = e.target.closest('tr[data-id]'); if(tr) location.href = 'factura-detalle.html?id=' + tr.dataset.id;
  });
  const vacio = (cols, t) => `<tr><td colspan="${cols}" style="text-align:center;padding:36px;color:var(--muted)">${t}</td></tr>`;

  /* ---------------- Resumen ---------------- */
  async function downloadPdf(path, filename) {
    const pdf = await portal.request(path, 'GET', undefined, 'application/pdf');
    const url = URL.createObjectURL(pdf);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  function renderDashboard(){
    action('#download-history', () => downloadPdf('/panel/reportes/procesos.pdf', 'procesos.pdf'));
    const docs = D.facturas;
    const year = new Date().getFullYear();
    const months = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
    const monthly = value => months.map((_, index) => docs.filter(f =>
      f.fecha.startsWith(value + '-' + String(index + 1).padStart(2,'0'))).reduce((sum,f) => sum + f.total,0)/1e6);
    const counts = keys => {
      const totals = new Map();
      docs.forEach(f => keys(f).forEach(key => totals.set(key, (totals.get(key) || 0) + 1)));
      return [...totals].sort((a,b) => b[1]-a[1]);
    };
    const sites = counts(f => [f.sede || 'Sin sede']);
    const methods = counts(f => f.methods);
    const payers = counts(f => f.payers);
    const colors = ['#9DB8EA','#16161D','#7DE3C9','#3CCBFF','#B79AF0','#7FD98B'];
    renderStats($('#stats'), [
      {tono:'lilac',etiqueta:'Documentos convertidos',valor:docs.length},
      {tono:'sky',etiqueta:'Neto en staging',valor:cop(docs.reduce((sum,f)=>sum+f.total,0))},
      {tono:'ice',etiqueta:'Clientes facturados',valor:new Set(docs.map(f=>f.nit)).size},
      {tono:'mauve',etiqueta:'Terceros de pago',valor:payers.length}
    ]);
    $('#chart-line').innerHTML = CHARTS.line(months, monthly(year), monthly(year-1), ' M');
    $('#rank-sedes').innerHTML = sites.map(([name,n]) =>
      `<li><span>${esc(name)}</span><span class="bar"><i style="width:${100*n/sites[0][1]}%"></i></span><span>${n}</span></li>`).join('') || 'Sin documentos';
    $('#chart-bars').innerHTML = methods.length ? CHARTS.bars(methods, colors) : 'Sin pagos registrados';
    const count = payers.reduce((sum,p)=>sum+p[1],0);
    const shares = payers.map(([name,n],i)=>[name, Math.round(n/count*1000)/10, colors[i%colors.length]]);
    $('#chart-donut').innerHTML = shares.length ? CHARTS.donut(shares) : 'Sin terceros registrados';
    $('#donut-legend').innerHTML = shares.map(([name,n,color]) =>
      `<div><i style="background:${color}"></i><span>${esc(name)}</span><span>${n} %</span></div>`).join('');
    $('#recent-documents').innerHTML = docs.slice(0,5).map(f =>
      `<li><a href="factura-detalle.html?id=${f.document_id}">${esc(f.numero || f.noext)}</a></li>`).join('') || 'Sin documentos';
    const tb = $('#tabla-recientes'); tb.innerHTML = D.facturas.slice(0,5).map(f=>filaFactura(f,false)).join('') || vacio(6,'Todavía no hay documentos convertidos.'); irDetalle(tb);
  }

  /* ---------------- Facturas ---------------- */
  async function eliminarFactura(id, numero) {
    if (!confirm('¿Eliminar la factura ' + numero + '? Esta acción no se puede deshacer.')) return;
    await portal.request('/staging/' + id, 'DELETE');
    location.href = 'facturas.html';
  }

  function renderFacturas(){
    $('#f-refrescar').onclick = () => location.reload();
    $('#f-tipo').replaceChildren(new Option('Todos los tipos',''), ...[...new Set(D.facturas.map(f => f.tipo))].map(type => new Option(type,type)));
    renderStats($('#stats-facturas'), [{tono:'lilac',etiqueta:'Documentos convertidos',valor:D.facturas.length}]);
    const tb = $('#tabla-facturas');
    tb.addEventListener('click', async event => {
      const button = event.target.closest('[data-eliminar-factura]');
      if (!button) return;
      const tr = button.closest('tr');
      button.disabled = true;
      try { await eliminarFactura(tr.dataset.id, tr.cells[0].textContent); }
      catch (error) { toast(error.message, 'err'); }
      finally { button.disabled = false; }
    });

    let visibles = D.facturas;   // NUEVO

    const pintar = () => {
      const q = $('#f-buscar').value.trim().toLowerCase(), est = $('#f-estado').value, tipo = $('#f-tipo').value;
      const rows = D.facturas.filter(f => (!est||f.estado===est) && (!tipo||f.tipo===tipo) &&
        (!q || [f.numero,f.noext,f.nit,f.cliente].join(' ').toLowerCase().includes(q)));
      tb.innerHTML = rows.length ? rows.map(f=>filaFactura(f,true)).join('') : vacio(8,'No hay facturas con esos filtros.');
      $('#f-conteo').textContent = `Mostrando ${rows.length} de ${D.facturas.length} facturas`;
    };
    // NUEVO: exportar CSV
    action('#f-exportar', () => {
      if (!visibles.length) throw new Error('No hay facturas para exportar.');
      const csv = c => `"${String(c ?? '').replace(/"/g, '""')}"`;
      const head = ['Número','Doc. externo','NIT','Cliente','Fecha','Tipo','Total','Estado'];
      const lines = visibles.map(f => [f.numero, f.noext, f.nit, f.cliente, f.fecha, f.tipo, f.total, f.estado].map(csv).join(';'));
      const blob = new Blob(['\uFEFF' + [head.map(csv).join(';'), ...lines].join('\r\n')], {type:'text/csv;charset=utf-8'});
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url; link.download = 'facturas.csv'; link.click();
      setTimeout(() => URL.revokeObjectURL(url), 1000);
    });
    ['input','change'].forEach(ev => { $('#f-buscar').addEventListener(ev,pintar); $('#f-estado').addEventListener(ev,pintar); $('#f-tipo').addEventListener(ev,pintar); });
    irDetalle(tb); pintar();
  }

  /* ---------------- Detalle de factura ---------------- */
  function renderDetalle(){
    const row = D.detail;
    action('#download-invoice', () => downloadPdf(
      '/panel/facturas/' + row.document_id + '/documento.pdf', 'factura-' + row.document_id + '.pdf'));
    if (!portalCan('MANAGE')) { $('#delete-invoice')?.remove(); $('#edit-invoice')?.remove(); }
    action('#delete-invoice', () => eliminarFactura(row.document_id, row.numero || row.noext));
    const editor = $('#dlg-factura'), input = $('#edit-invoice-json');
    action('#edit-invoice', async () => {
      const draft = await portal.request('/portal-data/documents/' + row.document_id + '/editor');
      input.value = draft.json;
      editor.querySelector('[role="alert"]')?.remove();
      editor.showModal();
    });
    action('#save-invoice', async () => {
      await portal.request('/staging/' + row.document_id, 'PUT', input.value);
      editor.close();
      location.reload();
    });
    if (new URLSearchParams(location.search).get('editar') === '1') {
      history.replaceState(null, '', 'factura-detalle.html?id=' + row.document_id);
      $('#edit-invoice')?.click();
    }
    const c = row.document.cliente;
    const f = {...row.document, cliente:row.cliente, total:row.total, sede:row.sede, estado:'validada'};
    const download = $('#download-json');
    download.onclick = () => {
      const url = URL.createObjectURL(new Blob([JSON.stringify(row.document,null,2)], {type:'application/json'}));
      const link = document.createElement('a');
      link.href = url; link.download = 'factura-' + row.document_id + '.json'; link.click();
      setTimeout(() => URL.revokeObjectURL(url),1000);
    };
    $('#d-numero').textContent = 'Factura ' + (f.numero || f.noext);
    $('#d-detalle').textContent = f.detalle;
    $('#d-estado').innerHTML = tagHTML(f.estado);
    const dato = (k,v) => `<div><span>${k}</span><b>${esc(v||'—')}</b></div>`;
    $('#d-info').innerHTML = dato('Cliente',f.cliente)+dato('Identificación',f.nit+(c&&c.dv?'-'+c.dv:''))+dato('Documento externo',f.noext)+
      dato('Tipo',f.tipo)+dato('Fecha',fechaCorta(f.fecha))+dato('Sede',f.sede)+dato('Documento afectado',f.afecta||'No aplica');
    $('#d-items').innerHTML = f.items.map(i=>`<tr><td>${esc(i.referencia)}</td><td>${esc(i.servicio)}</td><td class="r num">${esc(i.cant)}</td><td class="r num">${cop(i.vrunit)}</td><td class="r num">${esc(i.piva ?? 0)} %</td><td class="r num">${cop(i.vrtotal)}</td></tr>`).join('');
    $('#d-pagos').innerHTML = f.fpagos.map(p=>`<tr><td><span class="tag brand plain">${esc(p.fpago)}</span></td><td class="num">${fechaCorta(p.fechaven)}</td><td class="r num">${esc(p.cuota)}</td><td>${esc(p.tercero)}</td><td class="r num">${cop(p.vrfpago)}</td></tr>`).join('');
    const sub = f.items.reduce((s,i)=>s+i.vrtotal,0), iva = f.items.reduce((s,i)=>s+(i.vriva||0),0);
    $('#d-resumen').innerHTML = `<div><span>Subtotal</span><b class="num">${cop(sub)}</b></div><div><span>IVA</span><b class="num">${cop(iva)}</b></div><div class="total"><span>Total</span><b class="num">${cop(f.total)}</b></div>`;
  }

  /* ---------------- Enviar factura ---------------- */
  function renderEnviar(){
    const ta = $('#e-json'), av = $('#e-aviso');
    action('#e-ejemplo', async () => {
      const response = await fetch('ejemplo-ips.json');
      if (!response.ok) throw new Error('No se pudo cargar el ejemplo.');
      ta.value = JSON.stringify(await response.json(),null,2); av.hidden = true;
    });
    action('#e-enviar', async () => {
      av.hidden = false;
      av.textContent = 'Procesando…';
      try {
        const result = await portal.request('/staging','POST',ta.value);
        av.textContent = ['Solicitud ' + result.requestId, result.status, ...result.messages].join(' · ');
      } catch (error) { av.textContent = error.message; }
    });
    $('#e-limpiar').addEventListener('click', () => { ta.value = ''; av.hidden = true; });
    $('#e-archivo').addEventListener('change', e => {
      const file = e.target.files[0]; if(!file) return;
      const r = new FileReader(); r.onload = () => { ta.value = r.result; av.hidden = true; toast('Archivo cargado en el editor','ok'); };
      r.readAsText(file);
    });
  }

  /* ---------------- Consulta SysCafé ---------------- */
  function renderSyscafe(){
    const day = $('#g-fecha'), output = $('#g-json'), copy = $('#g-copiar');
    const now = new Date();
    day.valueAsDate = new Date(Date.UTC(now.getFullYear(),now.getMonth(),now.getDate()));
    copy.disabled = true;
    action('#g-consultar', async () => {
      copy.disabled = true;
      output.textContent = ''; $('#g-resumen').textContent = '';
      const page = $('#g-page');
      if (!day.value || !page.value || !page.checkValidity()) throw new Error('Revise la fecha y la página.');
      const rows = await portal.request('/GetFacturas?' + new URLSearchParams({fecha:day.value,consec:page.value}));
      output.textContent = JSON.stringify(rows,null,2);
      $('#g-resumen').textContent = rows.length + ' documento(s) en esta página';
      copy.disabled = false;
    });
    action('#g-copiar', async () => { await navigator.clipboard.writeText(output.textContent); toast('JSON copiado','ok'); });
  }

  /* ---------------- Clientes ---------------- */
  function renderClientes(){
    renderStats($('#stats-clientes'), [
      {tono:'lilac',etiqueta:'Clientes',valor:String(D.clientes.length)},
      {tono:'sky',etiqueta:'Con NIT',valor:String(D.clientes.filter(c=>c.claseid==='N').length)},
      {tono:'ice',etiqueta:'Personas naturales',valor:String(D.clientes.filter(c=>c.claseid!=='N').length)},
      {tono:'mauve',etiqueta:'Con facturas',valor:String(new Set(D.facturas.map(f=>f.nit)).size)}]);
    const tb = $('#tabla-clientes');
    const ini = n => n.split(' ').slice(0,2).map(w=>w[0]).join('').toUpperCase();
    const pintar = () => {
      const q = $('#c-buscar').value.trim().toLowerCase(), cl = $('#c-clase').value;
      const rows = D.clientes.filter(c => (!cl || c.claseid===cl) && (!q || [c.nit,c.nombrec,c.nom1,c.email].join(' ').toLowerCase().includes(q)));
      tb.innerHTML = rows.length ? rows.map(c=>`<tr data-key="${c.id}">
        <td><div style="display:flex;align-items:center;gap:12px"><span class="avatar" style="width:32px;height:32px;font-size:12px">${esc(ini(c.nombrec || c.nom1))}</span><div><span class="cell-main">${esc(c.nombrec || c.nom1)}</span><span class="cell-sub">${esc(c.dir)}</span></div></div></td>
        <td class="num">${esc(c.claseid)} · ${esc(c.nit)}${c.dv?'-'+esc(c.dv):''}</td><td>${esc(c.ciudad)}</td>
        <td>${esc(c.email)}<span class="cell-sub">${esc(c.tel)}</span></td>
        <td class="r num">${D.facturas.filter(f=>f.nit===c.nit).length}</td>
        <td class="actions">${portalCan('MANAGE') ? `<button class="icon-btn" aria-label="Editar">${ICON('edit')}</button><button class="icon-btn" aria-label="Eliminar">${ICON('trash')}</button>` : ''}</td></tr>`).join('')
        : vacio(6,'No hay clientes con esos filtros.');
    };
    $('#c-buscar').addEventListener('input',pintar); $('#c-clase').addEventListener('change',pintar); pintar();
  }

  /* ---------------- Equivalencias ---------------- */
  function renderEquivalencias(){
    const sel = $('#q-categoria');
    [...new Set(D.equivalencias.map(e=>e.categoria))].forEach(c => sel.insertAdjacentHTML('beforeend',`<option>${esc(c)}</option>`));
    const tb = $('#tabla-equiv');
    const pintar = () => {
      const q = $('#q-buscar').value.trim().toLowerCase(), cat = sel.value;
      const rows = D.equivalencias.filter(e => (!cat||e.categoria===cat) && (!q || Object.values(e).join(' ').toLowerCase().includes(q)));
      tb.innerHTML = rows.length ? rows.map(e=>`<tr data-key="${e.id}">
        <td><span class="tag brand plain">${esc(e.categoria)}</span></td>
        <td><code>${esc(e.campo_origen)}</code></td><td><code>${esc(e.campo_destino)}</code></td>
        <td>${e.valor_origen?`<b style="font-weight:500">${esc(e.valor_origen)}</b> <span style="color:var(--brand)">→</span> <b style="font-weight:500">${esc(e.valor_destino)}</b>`:'<span class="muted">Relación de campos</span>'}</td>
        <td class="actions"><button class="icon-btn" aria-label="Editar">${ICON('edit')}</button><button class="icon-btn" aria-label="Eliminar">${ICON('trash')}</button></td></tr>`).join('')
        : vacio(5,'No hay equivalencias con esos filtros.');
      $('#q-conteo').textContent = `Mostrando ${rows.length} de ${D.equivalencias.length} equivalencias`;
    };
    $('#q-buscar').addEventListener('input',pintar); sel.addEventListener('change',pintar); pintar();
  }

  function renderCuenta(){
    const user = sessionStorage.getItem('portal_user') || 'Operador';
    $('#account-user').value = user;
    $('#account-title').textContent = user;
    $('#cuenta-salir').onclick = () => portal.logout();
  }

  function action(selector, run) {
    const button = $(selector);
    if (!button) return; // el botón no existe para este rol
    button.onclick = async () => {
      button.disabled = true;
      try { await run(); } catch (error) {
        const dialog = button.closest('dialog');
        if (dialog) {
          let notice = dialog.querySelector('[role="alert"]');
          if (!notice) { notice = document.createElement('p'); notice.setAttribute('role','alert'); button.parentElement.before(notice); }
          notice.textContent = error.message;
        } else toast(error.message,'err');
      }
      finally { button.disabled = false; }
    };
  }

  function connectCatalog(resource, dialogId, table) {
    const dialog = $(dialogId), form = $('form',dialog);
    const rows = D[resource];
    const heading = $('h3',dialog), initialTitle = heading.textContent;
    let current;
    const reset = () => { current = null; heading.textContent = initialTitle; form.reset(); dialog.querySelector('[role="alert"]')?.remove(); };
    dialog.addEventListener('close', reset);
    $(table).addEventListener('click', async event => {
      const button = event.target.closest('button');
      if (!button) return;
      const id = Number(button.closest('tr').dataset.key);
      const row = rows.find(item => item.id === id);
      if (!row) return;
      if (button.getAttribute('aria-label') === 'Editar') {
        current = row;
        heading.textContent = resource === 'clientes' ? 'Editar cliente' : 'Editar equivalencia';
        for (const field of form.elements) if (field.name) field.value = row[field.name] ?? '';
        dialog.showModal();
      } else if (confirm('¿Eliminar este registro?')) {
        button.disabled = true;
        try { await portal.request('/'+resource+'/'+id,'DELETE'); location.reload(); }
        catch (error) { toast(error.message,'err'); button.disabled = false; }
      }
    });
    action(dialogId+' [data-save]', async () => {
      const data = {...current, ...Object.fromEntries(new FormData(form))};
      delete data.id;
      for (const key of Object.keys(data)) if (data[key] === '') delete data[key];
      await portal.request('/'+resource+(current ? '/'+current.id : ''), current ? 'PUT':'POST', JSON.stringify(data));
      location.reload();
    });
  }

  try {
    if (['dashboard','facturas','clientes'].includes(page)) {
      D.facturas = (await portal.request('/portal-data/documents')).sort((a,b)=>b.document_id-a.document_id);
      D.facturas.forEach(f => { f.estado = 'validada'; f.numero ||= f.noext; });
    }
    if (page === 'factura-detalle') {
      const id = new URLSearchParams(location.search).get('id');
      if (!/^[1-9][0-9]*$/.test(id || '')) throw new Error('Identificador de factura inválido.');
      D.detail = await portal.request('/portal-data/documents/'+id);
    }
    if (page === 'clientes' || page === 'equivalencias') D[page] = await portal.request('/'+page);
    ({dashboard:renderDashboard, facturas:renderFacturas, 'factura-detalle':renderDetalle, enviar:renderEnviar,
      syscafe:renderSyscafe, clientes:renderClientes, equivalencias:renderEquivalencias, cuenta:renderCuenta}[page])();
    if (page==='clientes') connectCatalog(page,'#dlg-cliente','#tabla-clientes');
    if (page==='equivalencias') connectCatalog(page,'#dlg-equiv','#tabla-equiv');
    if (!portalCan('MANAGE')) $$('[data-abrir="dlg-cliente"],[data-abrir="dlg-equiv"]').forEach(b => b.remove());
  } catch (error) {
    const notice = document.createElement('p');
    notice.className = 'errors'; notice.setAttribute('role','alert');
    notice.textContent = error.message;
    $('#contenido').prepend(notice);
  }
})();
