(function () {
  'use strict';

  const tokenKey = 'alunoOnlineToken';
  const nativeFetch = window.fetch.bind(window);
  const academicPath = /^\/(alunos|professores|disciplina|matriculas|dashboard|relatorios)(\/|$)/;
  const protectedPage = window.location.pathname !== '/login.html';
  let redirecting = false;

  function goToLogin(expired) {
    if (redirecting) return;
    redirecting = true;
    window.sessionStorage.removeItem(tokenKey);
    window.sessionStorage.removeItem('boletimAlunoId');
    window.top.location.replace('/login.html' + (expired ? '?motivo=sessao' : ''));
  }

  function checkSession() {
    if (protectedPage && !window.sessionStorage.getItem(tokenKey)) {
      goToLogin(false);
      return false;
    }
    return true;
  }

  window.AlunoAuth = {
    signIn(token) {
      window.sessionStorage.setItem(tokenKey, token);
      window.top.location.replace('/');
    },
    logout() {
      goToLogin(false);
    }
  };

  window.fetch = async function (input, options) {
    const url = new URL(input instanceof Request ? input.url : input, window.location.href);
    if (url.origin !== window.location.origin || !academicPath.test(url.pathname)) {
      return nativeFetch(input, options);
    }

    const token = window.sessionStorage.getItem(tokenKey);
    if (!token) {
      goToLogin(false);
      throw new Error('Entre na sua conta para continuar.');
    }

    // Init headers replace Request headers, just as they do in the Fetch API.
    const originalHeaders = options && options.headers !== undefined
      ? options.headers
      : (input instanceof Request ? input.headers : undefined);
    const headers = new Headers(originalHeaders);
    headers.set('Authorization', 'Bearer ' + token);
    const response = await nativeFetch(input, { ...options, headers });
    if (response.status === 401) goToLogin(true);
    return response;
  };

  document.addEventListener('click', function (event) {
    if (event.target.closest('[data-logout]')) {
      event.preventDefault();
      window.AlunoAuth.logout();
    }
  });

  // A restored page must recheck the session before reusing its in-memory data.
  window.addEventListener('pageshow', function (event) {
    if (checkSession() && protectedPage && event.persisted) window.location.reload();
  });

  checkSession();
})();
