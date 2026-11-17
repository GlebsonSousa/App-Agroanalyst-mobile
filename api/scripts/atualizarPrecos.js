try { process.loadEnvFile(); } catch {}
const { MongoClient } = require('mongodb');
const { atualizarPrecos } = require('../cepea');

(async () => {
  const cli = await MongoClient.connect(process.env.MONGODB_URI);
  console.log(await atualizarPrecos(cli.db(), { forcar: true }));
  await cli.close();
})().catch((e) => { console.error(e.message); process.exit(1); });
