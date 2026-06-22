const total = $input.first().json.totalAmount;

const fmt = (n) => new Intl.NumberFormat('es-DO', {
    style: 'currency',
    currency: 'DOP'
}).format(n);

const itbis = total * 0.18;
const totalConItbis = total + itbis;

const fecha = new Date($input.first().json.createdAt);

return [{
  json: {
    ...$input.all(),
    fechaFmt: fecha.toLocaleDateString('es-DO', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }),
    priceFmt: fmt($input.first().json.productPrice),
    subtotalFmt: fmt(total),
    itbisFmt: fmt(itbis),
    totalFinalFmt: fmt(totalConItbis)
  }
}];

