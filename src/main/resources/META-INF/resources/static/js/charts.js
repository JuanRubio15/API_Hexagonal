/* Gráficas SVG sin librerías externas. Cada función devuelve el SVG como texto. */
window.CHARTS = (function(){
  const nf = n => new Intl.NumberFormat('es-CO').format(n);

  function smooth(pts){
    let d = `M${pts[0][0]},${pts[0][1]}`;
    for(let i=0;i<pts.length-1;i++){
      const p0=pts[i-1]||pts[i], p1=pts[i], p2=pts[i+1], p3=pts[i+2]||p2, t=.18;
      d += ` C${p1[0]+(p2[0]-p0[0])*t},${p1[1]+(p2[1]-p0[1])*t} ${p2[0]-(p3[0]-p1[0])*t},${p2[1]-(p3[1]-p1[1])*t} ${p2[0]},${p2[1]}`;
    }
    return d;
  }

  function line(labels, actual, previo, unit){
    const W=640,H=240,L=42,R=14,T=14,B=30, max=Math.ceil(Math.max(1,...actual,...previo)/10)*10;
    const x=i=>L+i*(W-L-R)/(labels.length-1), y=v=>T+(H-T-B)*(1-v/max);
    const grid=[0,.25,.5,.75,1].map(f=>{const v=Math.round(max*f), yy=y(v);
      return `<line x1="${L}" x2="${W-R}" y1="${yy}" y2="${yy}" stroke="#E9E9F0" stroke-dasharray="3 4"/><text x="${L-8}" y="${yy+4}" text-anchor="end">${v}${unit||''}</text>`}).join('');
    const xl=labels.map((l,i)=>`<text x="${x(i)}" y="${H-8}" text-anchor="middle">${l}</text>`).join('');
    const pa=actual.map((v,i)=>[x(i),y(v)]), pp=previo.map((v,i)=>[x(i),y(v)]);
    const area=smooth(pa)+` L${x(labels.length-1)},${y(0)} L${x(0)},${y(0)} Z`;
    return `<svg class="chart" viewBox="0 0 ${W} ${H}" role="img" aria-label="Facturación mensual: año actual contra año previo">
      <defs><linearGradient id="gA" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#A914FF" stop-opacity=".16"/><stop offset="1" stop-color="#A914FF" stop-opacity="0"/></linearGradient></defs>
      ${grid}${xl}
      <path d="${area}" fill="url(#gA)"/>
      <path d="${smooth(pp)}" fill="none" stroke="#B9B9C8" stroke-width="2" stroke-dasharray="5 5" stroke-linecap="round"/>
      <path d="${smooth(pa)}" fill="none" stroke="#16161D" stroke-width="2.4" stroke-linecap="round"/>
      <circle cx="${pa[pa.length-1][0]}" cy="${pa[pa.length-1][1]}" r="4.5" fill="#fff" stroke="#A914FF" stroke-width="2.5"/>
    </svg>`;
  }

  function bars(data, colors){
    const W=360,H=210,L=34,R=6,T=10,B=28, max=Math.ceil(Math.max(...data.map(d=>d[1]))/10)*10, n=data.length;
    const bw=(W-L-R)/n, y=v=>T+(H-T-B)*(1-v/max);
    const grid=[0,.5,1].map(f=>{const v=Math.round(max*f);return `<line x1="${L}" x2="${W-R}" y1="${y(v)}" y2="${y(v)}" stroke="#E9E9F0" stroke-dasharray="3 4"/><text x="${L-6}" y="${y(v)+4}" text-anchor="end">${v}</text>`}).join('');
    const b=data.map((d,i)=>{const w=Math.min(26,bw*.6), x=L+i*bw+(bw-w)/2, yy=y(d[1]);
      return `<rect x="${x}" y="${yy}" width="${w}" height="${H-B-yy}" rx="7" fill="${colors[i%colors.length]}"><title>${esc(d[0])}: ${nf(d[1])}</title></rect><text x="${x+w/2}" y="${H-9}" text-anchor="middle">${esc(d[0].slice(0,8))}</text>`}).join('');
    return `<svg class="chart" viewBox="0 0 ${W} ${H}" role="img" aria-label="Facturas por forma de pago">${grid}${b}</svg>`;
  }

  function donut(data){
    const r=44, c=2*Math.PI*r; let off=0;
    const arcs=data.map(d=>{const len=c*d[1]/100, s=`<circle r="${r}" cx="60" cy="60" fill="none" stroke="${d[2]}" stroke-width="18" stroke-dasharray="${Math.max(len-2,0)} ${c}" stroke-dashoffset="${-off}" transform="rotate(-90 60 60)"><title>${esc(d[0])}: ${d[1]} %</title></circle>`; off+=len; return s}).join('');
    return `<svg viewBox="0 0 120 120" role="img" aria-label="Facturas por tipo de pagador" style="width:150px;height:150px"><circle r="${r}" cx="60" cy="60" fill="none" stroke="#EEEEF4" stroke-width="18"/>${arcs}</svg>`;
  }
  return {line,bars,donut};
})();
