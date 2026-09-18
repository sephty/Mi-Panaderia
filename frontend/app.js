const N8N_URL = 'http://localhost:5678/webhook/pasteleria-chat';
const BACKEND_URL = 'http://localhost:8080/api/pedidos';

const chatMessages = document.getElementById('chatMessages');
const chatForm = document.getElementById('chatForm');
const userInput = document.getElementById('userInput');
const typingIndicator = document.getElementById('typingIndicator');

const pedidoModal = document.getElementById('pedidoModal');
const closeModal = document.getElementById('closeModal');
const cancelModal = document.getElementById('cancelModal');
const formConfirmarPedido = document.getElementById('formConfirmarPedido');
const modalPastel = document.getElementById('modalPastel');
const modalFecha = document.getElementById('modalFecha');
const modalPorciones = document.getElementById('modalPorciones');
const modalNombre = document.getElementById('modalNombre');
const modalTelefono = document.getElementById('modalTelefono');
const modalEmail = document.getElementById('modalEmail');
const modalRestricciones = document.getElementById('modalRestricciones');

const btnVerPedidos = document.getElementById('btnVerPedidos');
const pedidosCocinaModal = document.getElementById('pedidosCocinaModal');
const closeCocinaModal = document.getElementById('closeCocinaModal');
const cocinaTableContainer = document.getElementById('cocinaTableContainer');

const fechaManana = new Date();
fechaManana.setDate(fechaManana.getDate() + 1);
modalFecha.min = fechaManana.toISOString().split('T')[0];
modalFecha.value = fechaManana.toISOString().split('T')[0];

chatForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const mensaje = userInput.value.trim();
  if (!mensaje) return;

  agregarMensajeUsuario(mensaje);
  userInput.value = '';
  mostrarEscribiendo(true);

  try {
    const res = await fetch(N8N_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mensaje: mensaje })
    });
    const data = await res.json();
    await new Promise(resolve => setTimeout(resolve, 600));
    mostrarEscribiendo(false);
    renderBot(data);
  } catch (error) {
    await new Promise(resolve => setTimeout(resolve, 500));
    mostrarEscribiendo(false);
    renderBot({
      texto: 'No se pudo conectar con n8n en ' + N8N_URL + '. Asegúrate de que el webhook esté activo.'
    });
  }
});

document.querySelectorAll('.pill').forEach(pill => {
  pill.addEventListener('click', () => {
    userInput.value = pill.dataset.prompt;
    chatForm.dispatchEvent(new Event('submit'));
  });
});

function agregarMensajeUsuario(texto) {
  const div = document.createElement('div');
  div.className = 'message user';
  div.innerHTML = `<div class="msg-bubble"><p>${escapeHtml(texto)}</p></div>`;
  chatMessages.appendChild(div);
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

function renderBot(data) {
  const div = document.createElement('div');
  div.className = 'message bot';

  let textoHtml = '';
  const textoCrudo = typeof data === 'string' ? data : (data.texto || data.respuesta || data.mensaje || '');
  if (textoCrudo) {
    textoHtml = `<p>${formatearTexto(textoCrudo)}</p>`;
  }

  let pastelesHtml = '';
  if (Array.isArray(data.pasteles) && data.pasteles.length > 0) {
    pastelesHtml = data.pasteles.map(p => `
      <div class="cake-card">
        <div class="cake-card-header">
          <span class="cake-title">${escapeHtml(p.nombre)}</span>
          <span class="cake-price">$${Number(p.precio_cop || 80000).toLocaleString('es-CO')} COP</span>
        </div>
        <p class="cake-desc">${escapeHtml(p.descripcion || '')}</p>
        <div class="tags-row">
          <span class="badge neutral">👥 ${p.porciones_min || 8}-${p.porciones_max || 12} porciones</span>
          ${p.vegano ? '<span class="badge safe">🌱 Vegano</span>' : ''}
          ${(p.alergenos && p.alergenos.length > 0)
            ? p.alergenos.map(a => `<span class="badge danger">⚠️ ${a}</span>`).join(' ')
            : '<span class="badge safe">✅ Libre de Alérgenos</span>'}
        </div>
        <button class="btn-reserve-cake" 
          onclick="abrirModal('${escapeHtml(p.nombre)}', ${p.porciones_min || 10}, '${(p.alergenos || []).join(', ')}')">
          Reservar este Pastel
        </button>
      </div>
    `).join('');
  }

  div.innerHTML = `
    <div class="msg-avatar">👩‍🍳</div>
    <div class="msg-bubble">
      ${textoHtml}
      ${pastelesHtml}
    </div>
  `;

  chatMessages.appendChild(div);
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

function formatearTexto(texto) {
  let limpio = texto.replace(/\/\/[^\n]*/g, '');
  limpio = escapeHtml(limpio);
  limpio = limpio.replace(/\*\*\*(.*?)\*\*\*/g, '<strong><em>$1</em></strong>');
  limpio = limpio.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  limpio = limpio.replace(/\*(.*?)\*/g, '<em>$1</em>');
  limpio = limpio.replace(/(?:^|\n)\s*-\s+/g, '<br>• ');
  limpio = limpio.replace(/\n\n/g, '</p><p>').replace(/\n/g, '<br>');
  return limpio;
}

function escapeHtml(texto) {
  if (!texto) return '';
  const div = document.createElement('div');
  div.textContent = texto;
  return div.innerHTML;
}

function mostrarEscribiendo(mostrar) {
  typingIndicator.style.display = mostrar ? 'flex' : 'none';
  if (mostrar) chatMessages.scrollTop = chatMessages.scrollHeight;
}

window.abrirModal = function(nombre, porciones, restricciones) {
  modalPastel.value = nombre;
  modalPorciones.value = porciones || 10;
  modalRestricciones.value = restricciones ? `Sin: ${restricciones}` : '';
  pedidoModal.style.display = 'flex';
};

closeModal.onclick = () => pedidoModal.style.display = 'none';
cancelModal.onclick = () => pedidoModal.style.display = 'none';

formConfirmarPedido.addEventListener('submit', async (e) => {
  e.preventDefault();

  const pedido = {
    clienteNombre: modalNombre.value,
    clienteTelefono: modalTelefono.value,
    clienteEmail: modalEmail.value,
    pastelNombre: modalPastel.value,
    fechaEntrega: modalFecha.value,
    porciones: parseInt(modalPorciones.value, 10),
    restricciones: modalRestricciones.value
  };

  try {
    const res = await fetch(BACKEND_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(pedido)
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.error || 'Error al procesar el pedido');

    pedidoModal.style.display = 'none';
    formConfirmarPedido.reset();

    renderBot({
      texto: `🎉 **¡Pedido #${data.id} Confirmado!**\nCliente: **${data.clienteNombre}**\nPastel: **${data.pastelNombre}** para el **${data.fechaEntrega}** (${data.porciones} porciones).\n${data.mensajeConfirmacion}`
    });

  } catch (error) {
    alert('No se pudo confirmar el pedido: ' + error.message);
  }
});

btnVerPedidos.onclick = async () => {
  pedidosCocinaModal.style.display = 'flex';
  cocinaTableContainer.innerHTML = '<p>Cargando pedidos de cocina...</p>';

  try {
    const res = await fetch(BACKEND_URL);
    const pedidos = await res.json();

    if (!pedidos || pedidos.length === 0) {
      cocinaTableContainer.innerHTML = '<p>No hay pedidos registrados en cocina todavía.</p>';
      return;
    }

    let tabla = `
      <table class="cocina-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Cliente</th>
            <th>Pastel</th>
            <th>Fecha</th>
            <th>Porciones</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
    `;

    pedidos.forEach(p => {
      tabla += `
        <tr>
          <td><strong>#${p.id}</strong></td>
          <td>${escapeHtml(p.clienteNombre)}<br><small>${escapeHtml(p.clienteTelefono || '')}</small></td>
          <td>${escapeHtml(p.pastelNombre)}</td>
          <td>${p.fechaEntrega}</td>
          <td>${p.porciones}</td>
          <td><span class="badge safe">${p.estado}</span></td>
        </tr>
      `;
    });

    tabla += '</tbody></table>';
    cocinaTableContainer.innerHTML = tabla;

  } catch (err) {
    cocinaTableContainer.innerHTML = '<p style="color:red;">Error al conectar con Spring Boot en ' + BACKEND_URL + '</p>';
  }
};

closeCocinaModal.onclick = () => pedidosCocinaModal.style.display = 'none';
