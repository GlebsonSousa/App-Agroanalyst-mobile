import os, re, subprocess, sys, time
import xml.etree.ElementTree as ET

resultados = []


def adb(*args):
    return subprocess.run(["adb", *args], capture_output=True).stdout.decode("utf-8", "replace")


def nos():
    adb("shell", "uiautomator", "dump", "/sdcard/u.xml")
    xml = adb("exec-out", "cat", "/sdcard/u.xml")
    try:
        raiz = ET.fromstring(xml[xml.index("<"):])
    except Exception:
        return []
    saida = []
    for n in raiz.iter("node"):
        b = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", n.get("bounds", ""))
        if not b:
            continue
        l, t, r, bt = map(int, b.groups())
        saida.append({"texto": n.get("text", ""), "desc": n.get("content-desc", ""), "x": (l + r) // 2, "y": (t + bt) // 2})
    return saida


def casa(n, sub, exato, campo):
    v = n[campo]
    return v == sub if exato else sub in v


def achar(sub, exato=False, ultimo=False, desc=False):
    lista = [n for n in nos() if casa(n, sub, exato, "desc" if desc else "texto")]
    if not lista:
        return None
    return max(lista, key=lambda n: n["y"]) if ultimo else lista[0]


def tem(sub, exato=False):
    return achar(sub, exato) is not None


def rolar():
    adb("shell", "input", "swipe", "180", "520", "180", "170", "350")
    time.sleep(1)


def achar_rolando(sub, exato=False, ultimo=False, desc=False, tentativas=6):
    for i in range(tentativas):
        n = achar(sub, exato, ultimo, desc)
        if n:
            return n
        rolar()
    return None


def tocar(sub, exato=False, ultimo=False, desc=False, rolando=True, espera=1.5):
    n = achar_rolando(sub, exato, ultimo, desc) if rolando else achar(sub, exato, ultimo, desc)
    if not n:
        return False
    adb("shell", "input", "tap", str(n["x"]), str(n["y"]))
    time.sleep(espera)
    return True


def digitar(texto):
    adb("shell", "input", "text", texto.replace(" ", "%s"))
    time.sleep(1)


def esperar(sub, segundos=40, exato=False):
    fim = time.time() + segundos
    while time.time() < fim:
        if tem(sub, exato):
            return True
        time.sleep(2)
    return False


def voltar():
    adb("shell", "input", "keyevent", "4")
    time.sleep(2)


def topo():
    for _ in range(5):
        adb("shell", "input", "swipe", "180", "200", "180", "560", "250")
    time.sleep(1)


def passo(nome, ok, detalhe=""):
    resultados.append(ok)
    print(("PASSOU " if ok else "FALHOU ") + nome + ((" :: " + detalhe) if detalhe and not ok else ""), flush=True)
    if not ok:
        textos = [n["texto"] for n in nos() if n["texto"]][:45]
        print("   tela agora:", textos, flush=True)


def aba(nome):
    return tocar(nome, exato=True, ultimo=True, rolando=False)


def campo(rotulo, valor):
    ok = tocar(rotulo, exato=False)
    if ok:
        digitar(valor)
    return ok



def foto(nome):
    os.makedirs("shots", exist_ok=True)
    dados = subprocess.run(["adb", "exec-out", "screencap", "-p"], capture_output=True).stdout
    with open(f"shots/{nome}.png", "wb") as f:
        f.write(dados)


def esperar_rolando(sub, segundos=60, exato=False):
    fim = time.time() + segundos
    while time.time() < fim:
        if achar_rolando(sub, exato, tentativas=4):
            return True
        time.sleep(3)
    return False



adb("shell", "pm", "grant", "com.example.agroanalyst", "android.permission.ACCESS_FINE_LOCATION")
adb("shell", "pm", "grant", "com.example.agroanalyst", "android.permission.ACCESS_COARSE_LOCATION")
adb("shell", "settings", "put", "secure", "location_mode", "3")
adb("emu", "geo", "fix", "-46.46", "-23.67")

# ---------------------------------------------------------------- Início
passo("Início abre com preços", esperar("Soja", 40))
passo("Início mostra o clima", esperar("Clima", 20))
ok = tocar("Escolher cidade", exato=True)
ok = ok and tocar("Buscar cidade", exato=True)
if ok:
    digitar("Campinas")
passo("Início: sugestões de cidade enquanto digita", ok and esperar("Campinas, SP", 45))
if tocar("Campinas, SP", exato=True):
    topo()
    passo("Início: clima troca para a cidade escolhida", esperar("Clima · Campinas, SP", 60))
else:
    passo("Início: clima troca para a cidade escolhida", False, "sugestão não encontrada")
topo()
foto("01_inicio")

# ---------------------------------------------------------------- Mercado
aba("Mercado")
passo("Mercado lista as culturas", esperar("Soja", 20) and esperar("CEPEA/ESALQ", 5))
if tocar("Soja", exato=True):
    passo("Mercado: toque abre o histórico", achar_rolando("Últimos") is not None)
foto("02_mercado")

# ---------------------------------------------------------------- Mapa (produção)
aba("Mapa")
passo("Mapa: camada de produção com legenda", esperar("Fonte: IBGE/SIDRA", 40))
time.sleep(8)
foto("03_mapa_producao")

# ---------------------------------------------------------------- Análise
aba("Análise")
passo("Análise: abre a tela de escolha", esperar("Escolha o ponto", 15))
ok = tocar("Usar minha posição", exato=True)
passo("Análise: GPS preenche o local", ok and esperar("Minha posição", 25), "plano B do sistema")
ok = tocar("Buscar cidade", exato=True)
if ok:
    digitar("Maua")
passo("Análise: sugestões de cidade", ok and esperar("Mauá, SP", 45))
if tocar("Mauá, SP", exato=True):
    passo("Análise: quadro mostra nome e coordenadas", esperar("−23,6678", 10) and esperar("Latitude", 5))
else:
    passo("Análise: quadro mostra nome e coordenadas", False)
ok = tocar("Analisar ponto", exato=True)
passo("Análise: resultado do solo carrega", ok and esperar("Argilossolo", 240), "consulta de solo demorou")
passo("Análise: pH 5,0 e não 0,0", tem("5,0", True) and not tem("0,0", True))
passo("Análise: chuva anual 1.336,6 mm", tem("1.336,6"))
passo("Análise: culturas recomendadas", tem("Arroz (Sequeiro)") or achar_rolando("Arroz (Sequeiro)") is not None)
passo("Análise: sem conta pede para entrar", achar_rolando("Entrar para salvar no plantio", True) is not None)
foto("04_analise_resultado")

if tocar("Arroz (Sequeiro)", exato=True):
    passo("Análise: detalhe da cultura abre", esperar("Faixas ideais e requisitos", 15))
    voltar()
else:
    passo("Análise: detalhe da cultura abre", False)

# ---------------------------------------------------------------- Login, plantio
ok = tocar("Entrar para salvar no plantio", exato=True)
passo("Login abre a partir de 'salvar'", ok and esperar("Entre para salvar plantios", 15))
tocar("E-mail", exato=True, rolando=False)
digitar("teste@fatec.br")
tocar("Senha", exato=True, rolando=False)
digitar("123456")
tocar("Entrar", exato=True, ultimo=True, rolando=False)
passo("Login volta para a análise", esperar("Salvar no plantio", 40, exato=True))
if tocar("Salvar no plantio", exato=True):
    passo("Formulário de plantio abre preenchido", esperar("Novo plantio", 15, exato=True) and tem("Arroz (Sequeiro)"))
    passo("Formulário mostra o local escolhido", achar_rolando("−23,6", False) is not None)
    topo()
    campo("Nome do plantio", "Talhao E2E")
    campo("Área (ha)", "10")
    campo("Produtividade (sacas/ha)", "50")
    foto("05_form_plantio")
    tocar("Salvar", exato=True, ultimo=True)
    passo("Plantio salvo e volta ao resultado", esperar("Salvar no plantio", 40, exato=True))
    voltar()
else:
    passo("Formulário de plantio abre preenchido", False)

aba("Meus")
passo("Meus: plantio aparece na lista", esperar("Talhao E2E", 40))
foto("06_lista_plantios")
if tocar("Talhao E2E", exato=True):
    passo("Detalhe do plantio abre", esperar("Fretes salvos", 40))
    passo("Detalhe mostra o solo do ponto", esperar("Argilossolo", 240))
    if tocar("Editar", exato=True):
        passo("Edição abre com os dados", esperar("Editar plantio", 20, exato=True) and esperar("Talhao E2E", 20))
        campo("Nome do plantio", "X")
        tocar("Salvar", exato=True, ultimo=True)
        passo("Edição salva e detalhe mostra o novo nome", esperar("Talhao E2EX", 40))
    else:
        passo("Edição abre com os dados", False)

# ---------------------------------------------------------------- Frete a partir do plantio
topo()
ok = tocar("Calcular frete", exato=True)
passo("Frete abre com a origem do plantio", ok and esperar("Local de partida", 20))
tocar("Porto de Santos", exato=False)
campo("Sacas de 60 kg", "3000")
campo("Preço por saca", "128.4")
foto("07_frete_formulario")
tocar("Calcular", exato=True, ultimo=True)
passo("Frete rodoviário calculado por rota", esperar_rolando("Distância por rota", 120))
passo("Frete mostra frete total", achar_rolando("Frete total") is not None)
foto("08_frete_resultado")
if tocar("Ver rota no mapa", exato=True):
    passo("Mapa abre com a rota real", esperar("Distância por rota:", 60))
    passo("Mapa mostra o trecho rodoviário", tem("Rodovia"))
    time.sleep(8)
    foto("09_mapa_rodovia")
    tocar("Ferroviário", exato=True, rolando=False)
    ferro = esperar("Ferrovia", 40) or esperar("indisponível", 5)
    passo("Mapa responde ao trocar para ferroviário", ferro)
    time.sleep(6)
    foto("10_mapa_ferrovia_ou_aviso")
    tocar("Hidroviário", exato=True, rolando=False)
    passo("Mapa: hidroviário indisponível não é desenhado", esperar("indisponível", 40))
    foto("11_mapa_hidrovia_aviso")

# ---------------------------------------------------------------- Frete com cidades, salvar local, ferroviário
aba("Início")
esperar("Calcular frete", 15)
tocar("Calcular frete", exato=True)
passo("Frete aberto pela Início", esperar("Local de partida", 20))
ok = tocar("Buscar cidade", exato=True)
if ok:
    digitar("Rondonopolis")
passo("Frete: sugestões para a partida", ok and esperar("Rondonópolis, MT", 45))
tocar("Rondonópolis, MT", exato=True)
passo("Frete: partida escolhida aparece no quadro", esperar("Rondonópolis, MT", 10))
if tocar("Salvar em meus locais", exato=True):
    tocar("Nome do local", exato=True, rolando=False)
    digitar("Partida E2E")
    tocar("Salvar", exato=True, ultimo=True, rolando=False)
    passo("Local salvo em meus locais", esperar_rolando("Partida E2E", 40))
tocar("Porto de Santos", exato=False)
campo("Sacas de 60 kg", "3000")
campo("Preço por saca", "128.4")
tocar("Ferroviário", exato=True)
foto("12_frete_ferroviario_form")
tocar("Calcular", exato=True, ultimo=True)
passo("Frete ferroviário por trajeto real", esperar_rolando("Distância por rota", 120))
if tocar("Ver rota no mapa", exato=True):
    passo("Mapa: rota ferroviária tem trecho de ferrovia", esperar("Ferrovia", 60) and tem("Distância por rota:"))
    time.sleep(8)
    foto("13_mapa_ferrovia_real")

# ---------------------------------------------------------------- limpeza
aba("Início")
tocar("Calcular frete", exato=True)
if tocar("Remover Partida E2E", desc=True):
    time.sleep(3)
    passo("Local removido", not tem("Partida E2E"))
voltar()
aba("Meus")
esperar("Talhao E2E", 40)
if tocar("Excluir plantio Talhao E2E", desc=True):
    tocar("Excluir", exato=True, ultimo=True, rolando=False)
    time.sleep(3)
    passo("Plantio excluído", not tem("Talhao E2E"))

total = len(resultados)
print(f"RESUMO: {sum(resultados)}/{total} passos", flush=True)
