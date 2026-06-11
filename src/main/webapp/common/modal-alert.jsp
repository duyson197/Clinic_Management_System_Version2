<%@ page pageEncoding="UTF-8" %>
<%-- Modal Alert & Confirm - dùng thay thế alert() và confirm() mặc định --%>
<style>
  .modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(15, 23, 42, 0.5);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    opacity: 0;
    visibility: hidden;
    transition: opacity 0.2s ease, visibility 0.2s ease;
  }
  .modal-overlay.is-open {
    opacity: 1;
    visibility: visible;
  }
  .modal-box {
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
    max-width: 400px;
    width: calc(100% - 32px);
    margin: 16px;
    transform: scale(0.95);
    transition: transform 0.2s ease;
    overflow: hidden;
    border: 1px solid rgba(226, 232, 240, 0.8);
  }
  .modal-overlay.is-open .modal-box {
    transform: scale(1);
  }
  .modal-box .modal-icon-wrap {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    font-size: 28px;
  }
  .modal-box .modal-icon-wrap.success { background: #dcfce7; color: #16a34a; }
  .modal-box .modal-icon-wrap.error   { background: #fee2e2; color: #dc2626; }
  .modal-box .modal-icon-wrap.warning { background: #fef3c7; color: #d97706; }
  .modal-box .modal-icon-wrap.info    { background: #dbeafe; color: #2563eb; }
  .modal-box .modal-icon-wrap.confirm { background: #e0e7ff; color: #4f46e5; }
  #customConfirmOverlay .modal-icon-wrap.confirm {
    margin: 24px auto 12px;
  }
  #customConfirmOverlay .modal-icon-wrap.confirm i {
    display: block;
    line-height: 1;
  }
  #customConfirmTitle {
    display: none;
  }
  #customConfirmOverlay .modal-body {
    padding-top: 0;
  }
  .modal-box .modal-body {
    padding: 8px 24px 24px;
    text-align: center;
  }
  .modal-box .modal-title {
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 8px;
  }
  .modal-box .modal-message {
    font-size: 14px;
    color: #64748b;
    line-height: 1.5;
    white-space: pre-line;
  }
  .modal-box .modal-actions {
    display: flex;
    gap: 12px;
    justify-content: center;
    padding: 0 24px 24px;
  }
  .modal-box .modal-actions.single {
    justify-content: center;
  }
  .modal-box .btn-modal {
    min-width: 100px;
    padding: 10px 20px;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    border: none;
    transition: all 0.2s;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
  }
  .modal-box .btn-modal-primary {
    background: #2563eb;
    color: #fff;
    box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);
  }
  .modal-box .btn-modal-primary:hover {
    background: #1d4ed8;
    transform: translateY(-1px);
  }
  .modal-box .btn-modal-outline {
    background: #fff;
    color: #475569;
    border: 1px solid #e2e8f0;
  }
  .modal-box .btn-modal-outline:hover {
    background: #f8fafc;
    border-color: #cbd5e1;
  }
  .modal-box .btn-modal-success {
    background: #16a34a;
    color: #fff;
  }
  .modal-box .btn-modal-success:hover {
    background: #15803d;
    transform: translateY(-1px);
  }

  /* Prompt modal - textarea */
  #customPromptOverlay .modal-box {
    max-width: 480px;
  }
  #customPromptOverlay .modal-icon-wrap.prompt {
    background: #f0fdf4;
    color: #16a34a;
  }
  #promptTextarea {
    width: 100%;
    min-height: 90px;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    padding: 10px 12px;
    font-size: 14px;
    font-family: inherit;
    resize: vertical;
    outline: none;
    transition: border-color 0.15s;
    box-sizing: border-box;
    color: #1e293b;
    line-height: 1.5;
  }
  #promptTextarea:focus {
    border-color: #2563eb;
    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
  }
  #promptCharCount {
    font-size: 11px;
    color: #94a3b8;
    text-align: right;
    margin-top: 4px;
  }
  #promptCharCount.over-limit {
    color: #dc2626;
    font-weight: 600;
  }
</style>

<div id="customAlertOverlay" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="customAlertTitle">
  <div class="modal-box">
    <div id="customAlertIconWrap" class="modal-icon-wrap success">
      <i id="customAlertIcon" class="fas fa-check-circle"></i>
    </div>
    <div class="modal-body">
      <div id="customAlertTitle" class="modal-title"></div>
      <div id="customAlertMessage" class="modal-message"></div>
    </div>
    <div id="customAlertActions" class="modal-actions single">
      <button type="button" id="customAlertOkBtn" class="btn-modal btn-modal-primary">
        <i class="fas fa-check"></i> <span id="customAlertOkLabel">Đóng</span>
      </button>
    </div>
  </div>
</div>

<div id="customPromptOverlay" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="customPromptTitle">
  <div class="modal-box">
    <div class="modal-icon-wrap prompt" style="margin: 24px auto 16px;">
      <i class="fas fa-pen"></i>
    </div>
    <div class="modal-body" style="padding-top: 0;">
      <div id="customPromptTitle" class="modal-title">Ghi chú</div>
      <div id="customPromptMessage" class="modal-message" style="margin-bottom: 12px;"></div>
      <textarea id="promptTextarea" placeholder="Nhập ghi chú..." maxlength="500"></textarea>
      <div id="promptCharCount">0 / 500</div>
    </div>
    <div class="modal-actions">
      <button type="button" id="customPromptCancelBtn" class="btn-modal btn-modal-outline">
        <i class="fas fa-times"></i> Hủy
      </button>
      <button type="button" id="customPromptOkBtn" class="btn-modal btn-modal-primary">
        <i class="fas fa-save"></i> Lưu ghi chú
      </button>
    </div>
  </div>
</div>

<div id="customConfirmOverlay" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="customConfirmTitle">
  <div class="modal-box">
    <div id="customConfirmIconWrap" class="modal-icon-wrap confirm">
      <i class="fas fa-question-circle"></i>
    </div>
    <div class="modal-body">
      <div id="customConfirmTitle" class="modal-title"></div>
      <div id="customConfirmMessage" class="modal-message"></div>
    </div>
    <div class="modal-actions">
      <button type="button" id="customConfirmCancelBtn" class="btn-modal btn-modal-outline">
        <i class="fas fa-times"></i> <span id="customConfirmCancelLabel">Hủy</span>
      </button>
      <button type="button" id="customConfirmOkBtn" class="btn-modal btn-modal-primary">
        <i class="fas fa-check"></i> <span id="customConfirmOkLabel">Đồng ý</span>
      </button>
    </div>
  </div>
</div>

<script charset="UTF-8">
(function() {
  var T = {
    close: 'Đóng',
    confirm: 'Đồng ý',
    cancel: 'Hủy',
    success: 'Thành công',
    error: 'Lỗi',
    warning: 'Cảnh báo',
    info: 'Thông báo'
  };

  var alertOverlay = document.getElementById('customAlertOverlay');
  var alertIconWrap = document.getElementById('customAlertIconWrap');
  var alertIcon = document.getElementById('customAlertIcon');
  var alertTitle = document.getElementById('customAlertTitle');
  var alertMessage = document.getElementById('customAlertMessage');
  var alertOkBtn = document.getElementById('customAlertOkBtn');
  var alertOkLabel = document.getElementById('customAlertOkLabel');

  var confirmOverlay = document.getElementById('customConfirmOverlay');
  var confirmTitleEl = document.getElementById('customConfirmTitle');
  var confirmMessage = document.getElementById('customConfirmMessage');
  var confirmCancelBtn = document.getElementById('customConfirmCancelBtn');
  var confirmOkBtn = document.getElementById('customConfirmOkBtn');
  var confirmCancelLabel = document.getElementById('customConfirmCancelLabel');
  var confirmOkLabel = document.getElementById('customConfirmOkLabel');

  if (alertOkLabel) alertOkLabel.textContent = T.close;
  if (confirmTitleEl) confirmTitleEl.textContent = '';
  if (confirmCancelLabel) confirmCancelLabel.textContent = T.cancel;
  if (confirmOkLabel) confirmOkLabel.textContent = T.confirm;

  var confirmCallback = null;
  var alertCloseCallback = null;

  function closeAlert() {
    alertOverlay.classList.remove('is-open');
    document.body.style.overflow = '';
    if (typeof alertCloseCallback === 'function') {
      alertCloseCallback();
      alertCloseCallback = null;
    }
  }

  function closeConfirm() {
    confirmOverlay.classList.remove('is-open');
    document.body.style.overflow = '';
    confirmCallback = null;
  }

  alertOkBtn.addEventListener('click', closeAlert);
  alertOverlay.addEventListener('click', function(e) {
    if (e.target === alertOverlay) closeAlert();
  });

  confirmCancelBtn.addEventListener('click', function() {
    if (typeof confirmCallback === 'function') confirmCallback(false);
    closeConfirm();
  });

  confirmOkBtn.addEventListener('click', function() {
    if (typeof confirmCallback === 'function') confirmCallback(true);
    closeConfirm();
  });

  confirmOverlay.addEventListener('click', function(e) {
    if (e.target === confirmOverlay) {
      if (typeof confirmCallback === 'function') confirmCallback(false);
      closeConfirm();
    }
  });

  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
      if (alertOverlay.classList.contains('is-open')) closeAlert();
      if (confirmOverlay.classList.contains('is-open')) {
        if (typeof confirmCallback === 'function') confirmCallback(false);
        closeConfirm();
      }
    }
  });

  window.showAlert = function(message, type, onClose) {
    type = type || 'info';
    alertCloseCallback = typeof onClose === 'function' ? onClose : null;

    var titles = {
      success: T.success,
      error: T.error,
      warning: T.warning,
      info: T.info
    };
    var icons = {
      success: 'fa-check-circle',
      error: 'fa-exclamation-circle',
      warning: 'fa-exclamation-triangle',
      info: 'fa-info-circle'
    };

    alertTitle.textContent = titles[type] || titles.info;
    alertMessage.textContent = message;
    alertIconWrap.className = 'modal-icon-wrap ' + type;
    alertIcon.className = 'fas ' + (icons[type] || icons.info);
    document.body.style.overflow = 'hidden';
    alertOverlay.classList.add('is-open');
    alertOkBtn.focus();
  };

  window.showConfirm = function(message, onConfirm, onCancel) {
    confirmMessage.textContent = message;
    confirmCallback = function(ok) {
      if (ok && typeof onConfirm === 'function') onConfirm();
      if (!ok && typeof onCancel === 'function') onCancel();
    };
    document.body.style.overflow = 'hidden';
    confirmOverlay.classList.add('is-open');
    confirmCancelBtn.focus();
  };

  // Prompt modal
  var promptOverlay = document.getElementById('customPromptOverlay');
  var promptTitleEl = document.getElementById('customPromptTitle');
  var promptMessageEl = document.getElementById('customPromptMessage');
  var promptTextarea = document.getElementById('promptTextarea');
  var promptCharCount = document.getElementById('promptCharCount');
  var promptCancelBtn = document.getElementById('customPromptCancelBtn');
  var promptOkBtn = document.getElementById('customPromptOkBtn');
  var promptCallback = null;

  function closePrompt() {
    promptOverlay.classList.remove('is-open');
    document.body.style.overflow = '';
    promptCallback = null;
  }

  promptTextarea.addEventListener('input', function() {
    var len = promptTextarea.value.length;
    promptCharCount.textContent = len + ' / 500';
    promptCharCount.classList.toggle('over-limit', len > 500);
    promptOkBtn.disabled = len > 500;
  });

  promptCancelBtn.addEventListener('click', function() {
    if (typeof promptCallback === 'function') promptCallback(null);
    closePrompt();
  });

  promptOkBtn.addEventListener('click', function() {
    if (promptOkBtn.disabled) return;
    if (typeof promptCallback === 'function') promptCallback(promptTextarea.value);
    closePrompt();
  });

  promptOverlay.addEventListener('click', function(e) {
    if (e.target === promptOverlay) {
      if (typeof promptCallback === 'function') promptCallback(null);
      closePrompt();
    }
  });

  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape' && promptOverlay.classList.contains('is-open')) {
      if (typeof promptCallback === 'function') promptCallback(null);
      closePrompt();
    }
  });

  // showPrompt(title, message, defaultValue, onSubmit)
  // onSubmit(value) - value is null if cancelled
  window.showPrompt = function(title, message, defaultValue, onSubmit) {
    promptTitleEl.textContent = title || 'Ghi chú';
    promptMessageEl.textContent = message || '';
    promptTextarea.value = defaultValue || '';

    var len = promptTextarea.value.length;
    promptCharCount.textContent = len + ' / 500';
    promptCharCount.classList.toggle('over-limit', len > 500);
    promptOkBtn.disabled = len > 500;

    promptCallback = typeof onSubmit === 'function' ? onSubmit : null;
    document.body.style.overflow = 'hidden';
    promptOverlay.classList.add('is-open');
    setTimeout(function() { promptTextarea.focus(); }, 50);
  };
})();
</script>