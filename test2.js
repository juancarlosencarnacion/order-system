const data = $input.first().json;

// Formato moneda DOP
const fmt = (n) => new Intl.NumberFormat('es-DO', {
  style: 'currency',
  currency: 'DOP'
}).format(n);

// Formato fecha
const fechaFmt = new Date(data.createdAt).toLocaleDateString('es-DO', {
  day: '2-digit',
  month: 'long',
  year: 'numeric'
});

// Generar filas de productos
// Asume que data.products es un array: [{ name, quantity, price }]
let subtotal = 0;
let filasHTML = '';

for (const item of data.details) {
  const lineTotal = item.quantity * item.price;
  subtotal += lineTotal;

  filasHTML += `
    <tr style="border-top:1px solid #e8e8e4;">
      <td width="44%" style="padding:16px 12px; font-size:14px; color:#1a1a18;">
        <span style="font-weight:bold; text-transform:capitalize;">${item.name}</span><br>
        <span style="font-size:12px; color:#888780;">Artículo de venta</span>
      </td>
      <td width="14%" align="center" style="padding:16px 12px;">
        <span style="display:inline-block; background-color:#f1efe8; border:1px solid #d3d1c7; border-radius:6px; padding:3px 10px; font-size:13px; color:#1a1a18;">${item.quantity}</span>
      </td>
      <td width="21%" align="right" style="padding:16px 12px; font-size:14px; color:#1a1a18;">${fmt(item.price)}</td>
      <td width="21%" align="right" style="padding:16px 12px; font-size:14px; font-weight:bold; color:#3b6d11;">${fmt(lineTotal)}</td>
    </tr>
  `;
}

const itbis = subtotal * 0.18;
const totalFinal = subtotal + itbis;

return [{
  json: {
    ...data,
    fechaFmt,
    filasProductos: filasHTML,
    subtotalFmt: fmt(subtotal),
    itbisFmt: fmt(itbis),
    totalFinalFmt: fmt(totalFinal)
  }
}];