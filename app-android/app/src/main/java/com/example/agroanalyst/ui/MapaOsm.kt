package com.example.agroanalyst.ui

import android.graphics.Color as CorAndroid
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.example.agroanalyst.data.LinhaLogistica
import com.example.agroanalyst.data.PracaDto
import com.example.agroanalyst.data.ProducaoUfDto
import com.example.agroanalyst.data.UfPoligono
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.io.File

// OSM bloqueia User-Agent genérico (ex.: com.example...): precisa identificar o app
private const val AGENTE = "AgroAnalystMobile/2.0 (projeto academico Fatec Maua)"

private fun marcador(densidade: Float, cor: Int, raio: Int): GradientDrawable = GradientDrawable().apply {
    shape = GradientDrawable.OVAL
    setColor(cor)
    setStroke((3 * densidade).toInt(), CorAndroid.WHITE)
    val lado = (raio * 2 * densidade).toInt()
    setSize(lado, lado)
}

@Composable
fun MapaOsm(
    ufs: List<UfPoligono>,
    producao: Map<String, ProducaoUfDto>,
    cortes: List<Double>,
    cores: List<Color>,
    corSemDado: Color,
    corFundo: Color,
    mostrarProducao: Boolean,
    mostrarLogistica: Boolean,
    pracas: List<PracaDto>,
    linhas: List<LinhaLogistica>?,
    corFerrovia: Color,
    corHidrovia: Color,
    corPraca: Color,
    corRota: Color,
    origem: PracaDto?,
    destino: PracaDto?,
    aoTocarUf: (String) -> Unit,
    aoTocarPraca: (PracaDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val chave = listOf(
        ufs.size, producao.hashCode(), cortes, mostrarProducao, mostrarLogistica,
        pracas.size, linhas?.size, origem?.nome, destino?.nome
    )
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Configuration.getInstance().apply {
                userAgentValue = AGENTE
                osmdroidBasePath = File(ctx.cacheDir, "osmdroid")
                osmdroidTileCache = File(ctx.cacheDir, "osmdroid/tiles")
            }
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                minZoomLevel = 3.0
                controller.setZoom(4.0)
                controller.setCenter(GeoPoint(-14.2, -51.9))
                overlayManager.tilesOverlay.loadingBackgroundColor = corFundo.toArgb()
                overlayManager.tilesOverlay.loadingLineColor = corFundo.toArgb()
                onResume()
            }
        },
        update = { mapa ->
            if (mapa.tag == chave) return@AndroidView
            mapa.tag = chave
            InfoWindow.closeAllInfoWindowsOn(mapa)
            val densidade = mapa.context.resources.displayMetrics.density
            mapa.overlays.clear()
            if (mostrarProducao) ufs.forEach { uf ->
                val dado = producao[uf.codigo]
                val cor = if (dado?.valorProducao == null) corSemDado else cores[faixaDe(dado.valorProducao, cortes).coerceIn(0, 4)]
                uf.aneis.forEach { anel ->
                    mapa.overlays.add(Polygon().apply {
                        points = anel.map { (lon, lat) -> GeoPoint(lat, lon) }
                        fillPaint.color = cor.copy(alpha = 0.8f).toArgb()
                        outlinePaint.color = CorAndroid.WHITE
                        outlinePaint.strokeWidth = densidade
                        setOnClickListener { _, _, _ -> aoTocarUf(uf.codigo); true }
                    })
                }
            }
            if (mostrarLogistica) {
                linhas?.forEach { l ->
                    mapa.overlays.add(Polyline().apply {
                        setPoints(l.pontos.map { (lon, lat) -> GeoPoint(lat, lon) })
                        outlinePaint.color = (if (l.tipo == "hidrovia") corHidrovia else corFerrovia).toArgb()
                        outlinePaint.strokeWidth = 2.5f * densidade
                    })
                }
                if (origem != null && destino != null) mapa.overlays.add(Polyline().apply {
                    setPoints(listOf(GeoPoint(origem.lat ?: 0.0, origem.lon ?: 0.0), GeoPoint(destino.lat ?: 0.0, destino.lon ?: 0.0)))
                    outlinePaint.color = corRota.toArgb()
                    outlinePaint.strokeWidth = 3f * densidade
                })
                pracas.forEach { p ->
                    val escolhida = p.nome == origem?.nome || p.nome == destino?.nome
                    mapa.overlays.add(Marker(mapa).apply {
                        position = GeoPoint(p.lat ?: 0.0, p.lon ?: 0.0)
                        title = p.nome
                        icon = marcador(densidade, (if (escolhida) corRota else corPraca).toArgb(), if (escolhida) 11 else 8)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setOnMarkerClickListener { m, v ->
                            InfoWindow.closeAllInfoWindowsOn(v)
                            m.showInfoWindow()
                            aoTocarPraca(p)
                            true
                        }
                    })
                }
            }
            mapa.invalidate()
        },
        onRelease = { it.onDetach() }
    )
}
