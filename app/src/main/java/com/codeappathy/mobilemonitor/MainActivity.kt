package com.codeappathy.mobilemonitor

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Project(val name:String,val emoji:String,val purpose:String,val progress:Float,val status:String,val features:List<String>,val next:List<String>)
data class StoreApp(val name:String,val emoji:String,val purpose:String,val version:String,val repository:String,val releaseStatus:String,val directApkUrl:String?=null)

private val storeApps = listOf(
    StoreApp("MobileMonitorApp","📱","開発状況と自作アプリを見守る","未確認","Code-Appathy/MobileMonitorApp","公開Releaseを確認します"),
    StoreApp("スケジュールメモ","📅","予定をカレンダーと一覧で管理する","1.0.0","Code-Appathy/ScheduleMemoryApp","配布APK v1.0.0","https://github.com/Code-Appathy/MobileMonitorApp/releases/download/v1.1.7/ScheduleMemoryApp-v1.0.0.apk")
)

data class PublishedRelease(val version: String, val url: String, val apkUrl: String?, val notes: String)

private fun downloadApk(context: Context, url: String, fileName: String) {
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle(fileName)
        .setDescription("APKをダウンロードしています")
        .setMimeType("application/vnd.android.package-archive")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    manager.enqueue(request)
    Toast.makeText(context, "ダウンロードを開始しました。完了通知からAPKを開いてください。", Toast.LENGTH_LONG).show()
}

private fun fetchRelease(repository: String): PublishedRelease? {
    val connection = URL("https://api.github.com/repos/$repository/releases/latest").openConnection() as HttpURLConnection
    connection.connectTimeout = 10000
    connection.readTimeout = 10000
    connection.setRequestProperty("Accept", "application/vnd.github+json")
    connection.setRequestProperty("User-Agent", "MobileMonitorApp")
    try {
        if (connection.responseCode == 404) return null
        if (connection.responseCode != 200) throw IllegalStateException("GitHub HTTP ${connection.responseCode}")
        val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        val assets = json.optJSONArray("assets")
        var apk: String? = null
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                    apk = asset.optString("browser_download_url").takeIf { it.startsWith("https://github.com/") }
                    break
                }
            }
        }
        return PublishedRelease(
            json.optString("tag_name", "不明"),
            json.optString("html_url", "https://github.com/$repository/releases"),
            apk,
            json.optString("body", "")
        )
    } finally {
        connection.disconnect()
    }
}

private val projects = listOf(
    Project("AppHubApp","🏠","アプリをまとめる",.82f,"主要機能を実装中",listOf("ユーザー管理","アプリ管理","アクセス権"),listOf("連携情報を共通化")),
    Project("FolderMapApp","🗺️","パソコンの中を地図にする",.65f,"主要機能を実装中",listOf("フォルダ解析","マップ表示","赤丸マーカー"),listOf("設定保存","AI置き場所提案")),
    Project("PictureCodeApp","🎨","アイデアから絵をつくる",.50f,"基本設計まで進行",listOf("画像イメージ選択","個性的なスタイル"),listOf("生成フロー実装")),
    Project("SecuriPCApp","🛡️","パソコンを安全に見守る",.78f,"重要項目を実装中",listOf("端末設定チェック","ネットワーク確認"),listOf("判定結果の統合")),
    Project("MacroNextApp","📊","Excelの仕事を助ける",.35f,"設計中",listOf("Excel単位のユーザー管理","アクセス権設計"),listOf("MD連携","実装開始"))
)

class MainActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState); setContent { MobileMonitor() } }
}

@Composable fun MobileMonitor(){
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Project?>(null) }
    MaterialTheme(colorScheme = lightColorScheme(primary=Color(0xFF6750A4), background=Color(0xFFFFFBFE))) {
        Surface(Modifier.fillMaxSize()) {
            if(selected != null) ProjectDetail(selected!!){ selected=null }
            else Column {
                Column(Modifier.padding(18.dp)) {
                    Text("Mobile Monitor", fontSize=28.sp, fontWeight=FontWeight.Bold)
                    Text("PCアプリのみんなを、スマホから見てみよう", color=Color.Gray)
                }
                TabRow(tab) { tab=it }
                when(tab){
                    0 -> ProgressScreen { selected=it }
                    1 -> MapScreen { selected=it }
                    2 -> EncyclopediaScreen { selected=it }
                    else -> StoreScreen()
                }
            }
        }
    }
}

@Composable fun TabRow(tab:Int,onTab:(Int)->Unit){
    val labels=listOf("📊 進みぐあい","🗺️ みんなの地図","📚 アプリ図鑑","🏪 マイアプリ")
    androidx.compose.material3.TabRow(selectedTabIndex=tab){ labels.forEachIndexed { i,s -> Tab(selected=tab==i,onClick={onTab(i)},text={Text(s,fontSize=11.sp)}) } }
}

@Composable fun ProgressScreen(open:(Project)->Unit){
    val avg=projects.map{it.progress}.average().toFloat()
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)){
        Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF3C4))){ Column(Modifier.padding(16.dp)){ Text("みんなの進みぐあい",fontWeight=FontWeight.Bold); Text("${(avg*100).toInt()}%",fontSize=34.sp,fontWeight=FontWeight.Bold); LinearProgressIndicator(progress={avg},Modifier.fillMaxWidth()) } }
        projects.forEach { p -> ProjectCard(p){open(p)} }
        Text("1.1.1ではストア基盤を追加。開発モニターのデータはまだサンプルで、次段階でGitHub/.apphub同期へ進みます。",fontSize=12.sp,color=Color.Gray)
    }
}

@Composable fun ProjectCard(p:Project,open:()->Unit){
    Card(Modifier.fillMaxWidth().clickable{open()},colors=CardDefaults.cardColors(containerColor=Color(0xFFF4F0FF))){ Column(Modifier.padding(16.dp)){ Row(verticalAlignment=Alignment.CenterVertically){Text(p.emoji,fontSize=30.sp);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(p.name,fontWeight=FontWeight.Bold);Text(p.purpose)};Text("${(p.progress*100).toInt()}%",fontWeight=FontWeight.Bold)};Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={p.progress},Modifier.fillMaxWidth());Text(p.status,fontSize=12.sp,color=Color.Gray,modifier=Modifier.padding(top=6.dp)) } }
}

@Composable fun MapScreen(open:(Project)->Unit){
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Text("みんなは、こんなふうにつながるよ",fontWeight=FontWeight.Bold,fontSize=20.sp);Spacer(Modifier.height(14.dp))
        FunNode("🏠 AppHubApp","みんなをまとめる"){open(projects[0])}; Text("│\\n├──── 情報・管理 ────┤",color=Color.Gray)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){ FunNode("🗺️ FolderMap","場所を整理"){open(projects[1])}; FunNode("🎨 PictureCode","絵をつくる"){open(projects[2])} }
        Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){ FunNode("🛡️ SecuriPC","安全を確認"){open(projects[3])}; FunNode("📊 MacroNext","Excelを助ける"){open(projects[4])} }
        Text("↓ 進捗や機能を知らせる ↓",Modifier.padding(16.dp),color=Color.Gray);FunNode("📱 MobileMonitor","みんなの様子を見る"){}
    }
}

@Composable fun FunNode(title:String,sub:String,click:()->Unit){ Card(Modifier.padding(4.dp).clickable{click()},colors=CardDefaults.cardColors(containerColor=Color(0xFFE8F5E9))){Column(Modifier.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(title,fontWeight=FontWeight.Bold);Text(sub,fontSize=12.sp)}} }

@Composable fun EncyclopediaScreen(open:(Project)->Unit){ Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("アプリ図鑑",fontSize=22.sp,fontWeight=FontWeight.Bold);Text("むずかしい言葉を使わずに、何をするアプリなのか見てみよう。",color=Color.Gray);projects.forEach{p->Card(Modifier.fillMaxWidth().clickable{open(p)},colors=CardDefaults.cardColors(containerColor=Color(0xFFE3F2FD))){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(p.emoji,fontSize=38.sp);Spacer(Modifier.width(12.dp));Column{Text(p.name,fontWeight=FontWeight.Bold);Text(p.purpose);Text("やさしく見る →",color=Color(0xFF6750A4),fontSize=12.sp)}}}} } }

@Composable fun ProjectDetail(p:Project,back:()->Unit){ Column(Modifier.verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){ Text("← もどる",Modifier.clickable{back()},color=Color(0xFF6750A4));Text("${p.emoji} ${p.name}",fontSize=28.sp,fontWeight=FontWeight.Bold);Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF3C4))){Column(Modifier.padding(16.dp)){Text("これはなに？",fontWeight=FontWeight.Bold);Text("${p.name}は、${p.purpose}ためのアプリです。パソコンでしている仕事を、もっと分かりやすく便利にする仲間です。")}};Text("できること",fontWeight=FontWeight.Bold);p.features.forEach{Text("✅ $it")};Text("これから",fontWeight=FontWeight.Bold);p.next.forEach{Text("🌱 $it")};Text("開発の進みぐあい ${(p.progress*100).toInt()}%",fontWeight=FontWeight.Bold);LinearProgressIndicator(progress={p.progress},Modifier.fillMaxWidth());Text("技術情報",fontWeight=FontWeight.Bold);Text("GitHub: Code-Appathy/${p.name}\\n共有規格: .apphub/project.json + GUIDE.md",fontSize=13.sp,color=Color.Gray) } }


@Composable fun StoreScreen(){
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var releases by remember { mutableStateOf<Map<String, PublishedRelease?>>(emptyMap()) }
    LaunchedEffect(refresh) {
        loading = true
        error = null
        try {
            releases = withContext(Dispatchers.IO) {
                storeApps.associate { app ->
                    app.repository to if (app.directApkUrl != null) PublishedRelease("v${app.version}", app.directApkUrl, app.directApkUrl, "署名済みAPK") else runCatching { fetchRelease(app.repository) }.getOrElse { throw it }
                }
            }
        } catch (e: Exception) {
            error = "GitHubの取得に失敗しました。通信状態を確認してください。"
        } finally {
            loading = false
        }
    }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)){
        Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFFFE0F0))){
            Column(Modifier.padding(16.dp)){
                Text("🏪 わたしのアプリ",fontSize=24.sp,fontWeight=FontWeight.Bold)
                Text("GitHub Releaseから最新版を確認できるよ！")
                Text("MobileMonitorApp 1.1.9",fontSize=12.sp,color=Color.Gray)
                Button(onClick={refresh++},enabled=!loading){ Text(if(loading) "確認中…" else "🔄 最新情報を確認") }
            }
        }
        if(error != null) Text(error!!,color=Color(0xFFB00020))
        storeApps.forEach { app ->
            val release = releases[app.repository]
            Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color(0xFFE8F5E9))){
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Row(verticalAlignment=Alignment.CenterVertically){
                        Text(app.emoji,fontSize=36.sp);Spacer(Modifier.width(12.dp))
                        Column { Text(app.name,fontSize=20.sp,fontWeight=FontWeight.Bold);Text(app.purpose) }
                    }
                    HorizontalDivider()
                    Text("GitHub: ${app.repository}",fontSize=12.sp)
                    Text(when {
                        loading && !releases.containsKey(app.repository) -> "☁️ 最新版を確認中"
                        release == null && error == null -> "🌱 公開Releaseはまだありません"
                        release == null -> "⚠️ 最新版を確認できません"
                        else -> "🏷️ 最新Release: ${release.version}"
                    })
                    if(release != null){
                        Text(if(release.apkUrl != null) "📦 APK公開済み" else "📭 APKはありません")
                        if(release.notes.isNotBlank()) Text(release.notes.take(240),fontSize=12.sp,maxLines=6)
                        Button(onClick={
                            val url = release.apkUrl ?: release.url
                            if(release.apkUrl != null && url.startsWith("https://github.com/")) {
                                val fileName = if (app.name == "スケジュールメモ") "ScheduleMemoryApp-v1.0.0.apk" else "MobileMonitorApp-${release.version.removePrefix("v")}.apk"
                                downloadApk(context, url, fileName)
                            } else if(url.startsWith("https://github.com/")) context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }) { Text(if(release.apkUrl != null) "APKをダウンロード" else "Releaseを見る") }
                    }
                }
            }
        }
        Text("APKはAndroidのDownloadManagerでDownloadsへ保存します。完了通知からAndroid標準のインストール確認へ進んでください。",fontSize=12.sp,color=Color.Gray)
    }
}
