/* =====================================================================
   DATOS DE DEMOSTRACIÓN  ·  NO SON DATOS REALES
   ---------------------------------------------------------------------
   Todo lo que está en este archivo solo sirve para que las vistas se vean
   completas durante la etapa de diseño. Está aislado a propósito:
   al conectar el backend se reemplaza cada bloque por la respuesta de la API.

   Los nombres de campo copian los de la API real para que el cambio sea directo:
     DEMO.clientes      <->  GET /clientes     (cliente_id, nit, dv, claseid, nom1, nombrec, dir, tel, email, ciudad)
     DEMO.facturas      <->  GET /staging      (document_id, tipo, numero, noext, fecha, nit, detalle, items, fpagos)
     DEMO.equivalencias <->  GET /equivalencias (id, categoria, campo_origen, campo_destino, valor_origen, valor_destino)
     DEMO.getFacturas   <->  GET /GetFacturas?fecha=AAAA-MM-DD
   ===================================================================== */
window.DEMO = (function(){
  const clientes = [
    {cliente_id:1,nit:'900123456',dv:'7',claseid:'N',nom1:'Nueva EPS S.A.',nombrec:'Nueva EPS S.A.',dir:'Cra 7 # 45-10, Bogotá',tel:'6011234567',email:'facturacion@nuevaeps.example',ciudad:'11001'},
    {cliente_id:2,nit:'860987654',dv:'3',claseid:'N',nom1:'Sanitas EPS',nombrec:'Sanitas EPS',dir:'Av. 19 # 114-65, Bogotá',tel:'6017654321',email:'cuentas@sanitas.example',ciudad:'11001'},
    {cliente_id:3,nit:'1049612345',dv:'',claseid:'C',nom1:'María',ape1:'Gómez',nombrec:'María Fernanda Gómez',dir:'Calle 20 # 8-15, Tunja',tel:'3105558899',email:'maria.gomez@example.com',ciudad:'15001'},
    {cliente_id:4,nit:'891800123',dv:'1',claseid:'N',nom1:'Seguros Bolívar ARL',nombrec:'Seguros Bolívar ARL',dir:'Cra 10 # 16-39, Bogotá',tel:'6013411777',email:'arl@bolivar.example',ciudad:'11001'},
    {cliente_id:5,nit:'74180555',dv:'',claseid:'C',nom1:'Carlos',ape1:'Pérez',nombrec:'Carlos Andrés Pérez',dir:'Cra 11 # 19-20, Duitama',tel:'3126667788',email:'carlos.perez@example.com',ciudad:'15238'},
    {cliente_id:6,nit:'800456789',dv:'2',claseid:'N',nom1:'Medimás EPS',nombrec:'Medimás EPS',dir:'Calle 72 # 10-07, Bogotá',tel:'6015550123',email:'cartera@medimas.example',ciudad:'11001'}
  ];

  const mk = (id,tipo,numero,noext,fecha,nit,detalle,estado,items,pagos,extra={}) =>
    Object.assign({document_id:id,tipo,numero,noext,fecha,nit,detalle,estado,items,fpagos:pagos},extra);

  const facturas = [
    mk(1,'FV1','FV-1048','HC-88231','2026-11-09','900123456','Consulta especializada y laboratorio','validada',
       [{referencia:'890271',servicio:'Consulta de medicina especializada',cant:1,vrunit:85000,vrtotal:85000,piva:0,vriva:0,vrico:0},
        {referencia:'902210',servicio:'Hemograma tipo IV',cant:1,vrunit:32000,vrtotal:32000,piva:0,vriva:0,vrico:0}],
       [{fpago:'TRF',fechaven:'2026-12-09',cuota:1,vrfpago:117000,tercero:'900123456',ctafpago:'11100505'}],
       {bodega:'NOR',sede:'Sede Norte'}),
    mk(2,'FV1','FV-1047','HC-88190','2026-11-09','860987654','Procedimiento ambulatorio','validada',
       [{referencia:'861801',servicio:'Artroscopia diagnóstica',cant:1,vrunit:1450000,vrtotal:1450000,piva:0,vriva:0,vrico:0}],
       [{fpago:'TRF',fechaven:'2026-12-09',cuota:1,vrfpago:1450000,tercero:'860987654',ctafpago:'11100505'}],
       {bodega:'CEN',sede:'Sede Centro'}),
    mk(3,'FV1','FV-1046','HC-88102','2026-11-08','1049612345','Consulta particular y recargo tarjeta','enviada',
       [{referencia:'890201',servicio:'Consulta de medicina general',cant:1,vrunit:60000,vrtotal:60000,piva:19,vriva:11400,vrico:0}],
       [{fpago:'TAR',fechaven:'2026-11-08',cuota:1,vrfpago:73200,tercero:'1049612345',ctafpago:'11200505'}],
       {bodega:'VIL',sede:'Sede Villanueva'}),
    mk(4,'NC1','NC-0112','HC-87950','2026-11-07','900123456','Nota crédito por glosa parcial','enviada',
       [{referencia:'890271',servicio:'Consulta de medicina especializada',cant:1,vrunit:85000,vrtotal:85000,piva:0,vriva:0,vrico:0}],
       [{fpago:'TRF',fechaven:'2026-11-07',cuota:1,vrfpago:85000,tercero:'900123456',ctafpago:'11100505'}],
       {bodega:'NOR',sede:'Sede Norte',afecta:'FV-1031'}),
    mk(5,'FV1','FV-1045','HC-87811','2026-11-06','891800123','Atención por accidente de trabajo','pendiente',
       [{referencia:'871121',servicio:'Radiografía de columna lumbar',cant:2,vrunit:70000,vrtotal:140000,piva:0,vriva:0,vrico:0},
        {referencia:'890301',servicio:'Consulta de urgencias',cant:1,vrunit:95000,vrtotal:95000,piva:0,vriva:0,vrico:0}],
       [{fpago:'CRE',fechaven:'2026-12-06',cuota:1,vrfpago:235000,tercero:'891800123',ctafpago:'13050505'}],
       {bodega:'CEN',sede:'Sede Centro'}),
    mk(6,'FV1','FV-1044','HC-87700','2026-11-05','74180555','Consulta y efectivo','validada',
       [{referencia:'890201',servicio:'Consulta de medicina general',cant:1,vrunit:60000,vrtotal:60000,piva:0,vriva:0,vrico:0}],
       [{fpago:'EFE',fechaven:'2026-11-05',cuota:1,vrfpago:60000,tercero:'74180555',ctafpago:'11050505'}],
       {bodega:'VIL',sede:'Sede Villanueva'}),
    mk(7,'FV1','FV-1043','HC-87655','2026-11-04','800456789','Hospitalización día cama','rechazada',
       [{referencia:'S12301',servicio:'Estancia hospitalaria (día)',cant:3,vrunit:410000,vrtotal:1230000,piva:0,vriva:0,vrico:0}],
       [{fpago:'TRF',fechaven:'2026-12-04',cuota:1,vrfpago:1230000,tercero:'800456789',ctafpago:'11100505'}],
       {bodega:'NOR',sede:'Sede Norte'})
  ];
  facturas.forEach(f=>{
    f.total = f.items.reduce((s,i)=>s+i.vrtotal+i.vriva,0);
    const c = clientes.find(c=>c.nit===f.nit); f.cliente = c ? c.nombrec : f.nit;
  });

  const equivalencias = [
    {id:1,categoria:'documentos',campo_origen:'factura.tipo_documento',campo_destino:'tipo',valor_origen:'factura_venta',valor_destino:'FV1'},
    {id:2,categoria:'documentos',campo_origen:'factura.tipo_documento',campo_destino:'tipo',valor_origen:'nota_credito',valor_destino:'NC1'},
    {id:3,categoria:'documentos',campo_origen:'factura.sede_atencion',campo_destino:'bodega',valor_origen:'Sede Villanueva',valor_destino:'VIL'},
    {id:4,categoria:'documentos',campo_origen:'factura.sede_atencion',campo_destino:'bodega',valor_origen:'Sede Centro',valor_destino:'CEN'},
    {id:5,categoria:'documentos',campo_origen:'factura.sede_atencion',campo_destino:'bodega',valor_origen:'Sede Norte',valor_destino:'NOR'},
    {id:6,categoria:'clientes',campo_origen:'paciente.tipo_identificacion',campo_destino:'claseid',valor_origen:'CC',valor_destino:'C'},
    {id:7,categoria:'clientes',campo_origen:'paciente.tipo_identificacion',campo_destino:'claseid',valor_origen:'NIT',valor_destino:'N'},
    {id:8,categoria:'forma_pago',campo_origen:'pagos.forma',campo_destino:'fpago',valor_origen:'Transferencia',valor_destino:'TRF'},
    {id:9,categoria:'forma_pago',campo_origen:'pagos.forma',campo_destino:'fpago',valor_origen:'Efectivo',valor_destino:'EFE'},
    {id:10,categoria:'forma_pago',campo_origen:'pagos.forma',campo_destino:'fpago',valor_origen:'Tarjeta',valor_destino:'TAR'},
    {id:11,categoria:'items',campo_origen:'atenciones.codigo_servicio',campo_destino:'referencia',valor_origen:'CONS-GEN',valor_destino:'890201'},
    {id:12,categoria:'ajustes',campo_origen:'recargos.concepto',campo_destino:'cpto',valor_origen:'Recargo tarjeta',valor_destino:'RT1'}
  ];

  /* Cifras del dashboard (inventadas para la maqueta) */
  const dashboard = {
    stats:[
      {tono:'lilac',etiqueta:'Facturas del mes',valor:'1.248',delta:'+11,01 %',sube:true},
      {tono:'mauve',etiqueta:'Clientes activos',valor:'342',delta:'-0,03 %',sube:false},
      {tono:'sky',  etiqueta:'Equivalencias',valor:'156',delta:'+15,03 %',sube:true},
      {tono:'ice',  etiqueta:'Ingresos facturados',valor:'$48,2 M',delta:'+6,08 %',sube:true}
    ],
    meses:['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov'],
    anioActual:[14,18,15,24,20,26,22,29,25,31,36],
    anioPrevio:[10,12,17,14,19,16,20,18,23,21,25],
    sedes:[['Sede Norte',420],['Sede Centro',360],['Sede Villanueva',250],['Urgencias',140],['Laboratorio',78]],
    formasPago:[['Transferencia',31],['Crédito',24],['Efectivo',11],['Tarjeta',16],['Cheque',7],['Otros',21]],
    pagadores:[['EPS',52.1,'#16161D'],['Particulares',28.6,'#2F7BEA'],['ARL',13.3,'#3CCBFF'],['Otros',6.0,'#D9D9E3']]
  };

  const notificaciones = [
    {i:'check',t:'Factura FV-1048 validada',h:'Justo ahora'},
    {i:'users',t:'Nuevo cliente registrado',h:'hace 59 min'},
    {i:'send', t:'FV-1046 enviada a SysCafé',h:'hace 12 h'},
    {i:'swap', t:'Equivalencia «Sede Norte» actualizada',h:'Hoy, 11:59'}
  ];
  const actividad = [
    {a:'AD',t:'Se cargó el JSON de FV-1048',h:'Justo ahora'},
    {a:'SY',t:'SysCafé consultó las facturas del día',h:'hace 30 min'},
    {a:'AD',t:'Se editó el cliente Nueva EPS',h:'hace 3 h'},
    {a:'AD',t:'Se agregó la equivalencia «Tarjeta»',h:'Ayer, 04:15'},
    {a:'AD',t:'Se rechazó FV-1043 por validación',h:'8 nov 2026'}
  ];

  const getFacturas = {
    fecha:'2026-11-09',
    documentos:[{
      tipo:'FV1', numero:'FV-1048', noext:'HC-88231', fecha:'2026-11-09', bodega:'NOR',
      cliente:{nit:'900123456',dv:'7',claseid:'N',nombrec:'Nueva EPS S.A.',ciudad:'11001'},
      items:[{referencia:'890271',servicio:'Consulta de medicina especializada',cant:1,vrunit:85000,vrtotal:85000},
             {referencia:'902210',servicio:'Hemograma tipo IV',cant:1,vrunit:32000,vrtotal:32000}],
      fpagos:[{fpago:'TRF',fechaven:'2026-12-09',cuota:1,vrfpago:117000}],
      ajustes:[], datos_salud:{modalidad_pago:'Evento',numero_contrato:'CT-2026-114'}
    }]
  };

  const ejemploIPS = {
    sistema_origen:'SoftwareIPS',
    factura:{numero_interno:'FV-2001',tipo_documento:'factura_venta',fecha_emision:'2026-11-10',fecha_vencimiento:'2026-12-10',sede_atencion:'Sede Centro'},
    paciente:{tipo_identificacion:'NIT',numero_identificacion:'900123456',digito_verificacion:'7',nombre:'Nueva EPS S.A.'},
    atenciones:[{codigo_servicio:'CONS-GEN',nombre_servicio:'Consulta de medicina general',cantidad:1,valor_unitario:60000,porcentaje_iva:0}],
    pagos:[{forma:'Transferencia',valor:60000}]
  };

  return {clientes,facturas,equivalencias,dashboard,notificaciones,actividad,getFacturas,ejemploIPS};
})();
