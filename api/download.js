const { Readable } = require('stream');

const LANCAMENTO = 'https://api.github.com/repos/GlebsonSousa/app-agroanalyst-mobile/releases/latest';
const ARQUIVO = 'app-agro.apk';
const CINCO_MINUTOS = 5 * 60 * 1000;

let cache = null;

async function localizar() {
  if (cache && Date.now() - cache.em < CINCO_MINUTOS) return cache;
  const r = await fetch(LANCAMENTO, {
    headers: { 'User-Agent': 'AgroAnalystMobile/2.0', Accept: 'application/vnd.github+json' },
    signal: AbortSignal.timeout(15000)
  });
  if (!r.ok) throw new Error(`GitHub HTTP ${r.status}`);
  const asset = (await r.json()).assets?.find((a) => a.name === ARQUIVO);
  if (!asset) throw new Error('asset ausente');
  cache = { url: asset.browser_download_url, tamanho: asset.size, em: Date.now() };
  return cache;
}

async function baixarApk(req, res) {
  try {
    const a = await localizar();
    const cabecalhos = {
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Disposition': `attachment; filename="${ARQUIVO}"`,
      'Content-Length': String(a.tamanho)
    };
    if (req.method === 'HEAD') return res.set(cabecalhos).end();
    const origem = await fetch(a.url, { signal: AbortSignal.timeout(120000) });
    if (!origem.ok || !origem.body) throw new Error(`download HTTP ${origem.status}`);
    res.set(cabecalhos);
    Readable.fromWeb(origem.body).on('error', () => res.destroy()).pipe(res);
  } catch (e) {
    cache = null;
    if (!res.headersSent) res.status(503).json({ mensagem: 'APK ainda não publicado' });
    else res.destroy();
  }
}

module.exports = { baixarApk };
