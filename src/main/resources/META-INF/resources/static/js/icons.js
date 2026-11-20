/* Set de iconos único (trazo de 1.7 px) para toda la interfaz. Uso: ICON('doc') */
(function(){
  const P = {
    home:'<path d="M3 11l9-8 9 8"/><path d="M5 10v10h5v-6h4v6h5V10"/>',
    doc:'<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h6"/>',
    send:'<path d="M21 3L10 14"/><path d="M21 3l-7 18-4-7-7-4z"/>',
    search:'<circle cx="11" cy="11" r="7"/><path d="M20 20l-3.5-3.5"/>',
    users:'<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0"/><path d="M16 4.6a3.5 3.5 0 0 1 0 6.8M18 14.2a6.5 6.5 0 0 1 3.5 5.8"/>',
    swap:'<path d="M7 4L3 8l4 4M3 8h14"/><path d="M17 12l4 4-4 4M21 16H7"/>',
    user:'<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
    cloud:'<path d="M7 18a4.5 4.5 0 0 1-.6-8.96A6 6 0 0 1 18 8.5 4.8 4.8 0 0 1 17.5 18z"/>',
    bell:'<path d="M6 9a6 6 0 0 1 12 0c0 6 2.5 7.5 2.5 7.5h-17S6 15 6 9z"/><path d="M10 20a2 2 0 0 0 4 0"/>',
    sun:'<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>',
    clock:'<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    panel:'<rect x="3" y="4" width="18" height="16" rx="3"/><path d="M9 4v16"/>',
    panelr:'<rect x="3" y="4" width="18" height="16" rx="3"/><path d="M15 4v16"/>',
    star:'<path d="M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z"/>',
    plus:'<path d="M12 5v14M5 12h14"/>',
    download:'<path d="M12 4v11M7 11l5 5 5-5M5 20h14"/>',
    upload:'<path d="M12 16V5M7 9l5-5 5 5M5 20h14"/>',
    filter:'<path d="M4 5h16l-6 7v6l-4 2v-8z"/>',
    eye:'<path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/>',
    eyeoff:'<path d="M3 3l18 18"/><path d="M10.6 6.1A9.8 9.8 0 0 1 12 6c6.4 0 10 6 10 6a17 17 0 0 1-3.1 3.8M6.5 7.6A17 17 0 0 0 2 12s3.6 6 10 6c1.5 0 2.8-.3 4-.9"/><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2"/>',
    edit:'<path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16z"/><path d="M13.5 6.5l4 4"/>',
    trash:'<path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v5M14 11v5"/>',
    check:'<path d="M5 12.5l4.5 4.5L19 7.5"/>',
    x:'<path d="M6 6l12 12M18 6L6 18"/>',
    chevd:'<path d="M6 9l6 6 6-6"/>',
    chevr:'<path d="M9 6l6 6-6 6"/>',
    chevl:'<path d="M15 6l-6 6 6 6"/>',
    logout:'<path d="M15 4h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-3M10 17l-5-5 5-5M5 12h11"/>',
    up:'<path d="M3 17l6-6 4 4 8-8"/><path d="M15 7h6v6"/>',
    down:'<path d="M3 7l6 6 4-4 8 8"/><path d="M15 17h6v-6"/>',
    cal:'<rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/>',
    copy:'<rect x="9" y="9" width="11" height="11" rx="2"/><path d="M5 15V6a2 2 0 0 1 2-2h8"/>',
    mail:'<rect x="3" y="5" width="18" height="14" rx="3"/><path d="M3.5 7l8.5 6 8.5-6"/>',
    phone:'<path d="M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2z"/>',
    shield:'<path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z"/><path d="M9 12l2 2 4-4"/>',
    menu:'<path d="M4 7h16M4 12h16M4 17h16"/>',
    more:'<circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/>',
    bag:'<path d="M5 8h14l-1 12H6z"/><path d="M9 8V6a3 3 0 0 1 6 0v2"/>',
    receipt:'<path d="M6 3h12v18l-3-2-3 2-3-2-3 2z"/><path d="M9 8h6M9 12h6"/>',
    key:'<circle cx="8" cy="15" r="4"/><path d="M11 12l9-9M16 7l3 3"/>'
  };
  window.ICON = (n, cls='') => `<svg class="ico ${cls}" viewBox="0 0 24 24" aria-hidden="true">${P[n]||''}</svg>`;

  /* Iconos de marca (decorativos): rellenos */
  window.BRAND = {
    google:'<svg viewBox="0 0 24 24" aria-hidden="true"><path fill="#4285F4" d="M23 12.27c0-.8-.07-1.56-.2-2.3H12v4.35h6.17a5.28 5.28 0 0 1-2.29 3.46v2.87h3.7c2.17-2 3.42-4.94 3.42-8.38z"/><path fill="#34A853" d="M12 24c3.1 0 5.7-1.03 7.6-2.79l-3.7-2.87c-1.03.69-2.35 1.1-3.9 1.1-3 0-5.54-2.02-6.45-4.74H1.73v2.97A11.5 11.5 0 0 0 12 24z"/><path fill="#FBBC05" d="M5.55 14.7a6.9 6.9 0 0 1 0-4.4V7.33H1.73a11.5 11.5 0 0 0 0 10.34z"/><path fill="#EA4335" d="M12 5.56c1.69 0 3.2.58 4.4 1.72l3.3-3.3A11.07 11.07 0 0 0 12 .5 11.5 11.5 0 0 0 1.73 7.33l3.82 2.97C6.46 7.58 9 5.56 12 5.56z"/></svg>',
    facebook:'<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 2a10 10 0 0 0-1.6 19.9v-7H7.9V12h2.5V9.8c0-2.5 1.5-3.9 3.8-3.9 1.1 0 2.2.2 2.2.2v2.5h-1.3c-1.2 0-1.6.8-1.6 1.6V12h2.8l-.4 2.9h-2.4v7A10 10 0 0 0 12 2z"/></svg>',
    twitter:'<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M22 5.9a8.2 8.2 0 0 1-2.4.7 4.1 4.1 0 0 0 1.8-2.3 8.2 8.2 0 0 1-2.6 1 4.1 4.1 0 0 0-7 3.7A11.6 11.6 0 0 1 3.4 4.7a4.1 4.1 0 0 0 1.3 5.5 4.1 4.1 0 0 1-1.9-.5v.1a4.1 4.1 0 0 0 3.3 4 4.1 4.1 0 0 1-1.8.1 4.1 4.1 0 0 0 3.8 2.8A8.3 8.3 0 0 1 2 18.4a11.6 11.6 0 0 0 6.3 1.8c7.5 0 11.7-6.3 11.7-11.7v-.5A8.3 8.3 0 0 0 22 5.9z"/></svg>',
    instagram:'<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 2h10a5 5 0 0 1 5 5v10a5 5 0 0 1-5 5H7a5 5 0 0 1-5-5V7a5 5 0 0 1 5-5zm0 2a3 3 0 0 0-3 3v10a3 3 0 0 0 3 3h10a3 3 0 0 0 3-3V7a3 3 0 0 0-3-3zm5 3.5A4.5 4.5 0 1 1 7.5 12 4.5 4.5 0 0 1 12 7.5zm0 2A2.5 2.5 0 1 0 14.5 12 2.5 2.5 0 0 0 12 9.5zM17.2 5.8a1.1 1.1 0 1 1-1.1 1.1 1.1 1.1 0 0 1 1.1-1.1z"/></svg>',
    linkedin:'<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4.5 3A2.5 2.5 0 1 0 4.5 8 2.5 2.5 0 0 0 4.5 3zM2.3 9.5h4.4V21H2.3zM9.5 9.5h4.2v1.6h.1a4.6 4.6 0 0 1 4.1-2.2c4.4 0 5.2 2.9 5.2 6.600V21h-4.4v-4.600c0-1.100 0-2.500-1.500-2.500s-1.800 1.200-1.800 2.400V21H9.500z"/></svg>',
    dribbble:'<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" aria-hidden="true"><circle cx="12" cy="12" r="9.5"/><path d="M5 5c4 2 9 3 14.500 1.500M2.800 11c5 .5 10 2.500 12.500 9M8 21.200c1.500-5.500 5-9.500 12-9.600"/></svg>',
    behance:'<svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M3 5h5.500c2.500 0 3.800 1.100 3.800 3 0 1.300-.7 2.200-1.800 2.600 1.500.4 2.300 1.500 2.300 3 0 2.100-1.600 3.400-4 3.400H3zm2.200 4.700h2.900c1 0 1.600-.5 1.600-1.300s-.6-1.300-1.600-1.300H5.200zm0 5.600h3.200c1.100 0 1.800-.6 1.800-1.500s-.7-1.500-1.800-1.500H5.200zM15 7h5v1.200h-5zm2.600 2.800c2.300 0 3.700 1.600 3.700 4v.7h-5.200c.1 1.100.8 1.700 1.800 1.700.8 0 1.300-.3 1.600-.9h1.700c-.4 1.600-1.700 2.500-3.400 2.500-2.200 0-3.800-1.500-3.800-3.900s1.600-4.100 3.600-4.100zm-1.700 3.200h3.400c-.1-1-.7-1.600-1.700-1.600-.9 0-1.500.5-1.700 1.600z"/></svg>'
  };
})();
