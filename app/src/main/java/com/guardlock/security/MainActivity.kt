package com.guardlock.security

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var galleryContainer: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("guard_prefs", Context.MODE_PRIVATE)
        val rootScroll = ScrollView(this)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 64)
        }
        val title = TextView(this).apply { text = "Guard Lock Pro (Android 16)"; textSize = 22f }
        val inputToken = EditText(this).apply { hint = "Masukkan Telegram Bot Token"; setText(prefs.getString("bot_token", "")) }
        val inputChatId = EditText(this).apply { hint = "Masukkan Telegram Chat ID"; setText(prefs.getString("chat_id", "")) }
        val checkSaveLocal = CheckBox(this).apply { text = "Simpan Foto di Brankas Aplikasi"; isChecked = prefs.getBoolean("save_local", true) }
        val checkSendTelegram = CheckBox(this).apply { text = "Kirim Foto dan Lokasi ke Telegram"; isChecked = prefs.getBoolean("send_telegram", true) }
        val btnSaveConfig = Button(this).apply {
            text = "Simpan Konfigurasi dan Aktifkan Izin"
            setOnClickListener {
                prefs.edit().putString("bot_token", inputToken.text.toString().trim()).putString("chat_id", inputChatId.text.toString().trim()).putBoolean("save_local", checkSaveLocal.isChecked).putBoolean("send_telegram", checkSendTelegram.isChecked).apply()
                requestPermissionsAndAdmin()
                Toast.makeText(context, "Pengaturan berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
        }
        val btnClearVault = Button(this).apply {
            text = "Hapus Semua Foto di Aplikasi"
            setOnClickListener {
                File(filesDir, "intruders").listFiles()?.forEach { it.delete() }
                loadVaultPhotos()
                Toast.makeText(context, "Brankas aplikasi dibersihkan", Toast.LENGTH_SHORT).show()
            }
        }
        val vaultTitle = TextView(this).apply { text = "\nGaleri Penyusup (Tersimpan di Aplikasi):"; textSize = 18f }
        galleryContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        mainLayout.addView(title); mainLayout.addView(inputToken); mainLayout.addView(inputChatId); mainLayout.addView(checkSaveLocal); mainLayout.addView(checkSendTelegram); mainLayout.addView(btnSaveConfig); mainLayout.addView(btnClearVault); mainLayout.addView(vaultTitle); mainLayout.addView(galleryContainer)
        rootScroll.addView(mainLayout); setContentView(rootScroll); loadVaultPhotos()
    }
    override fun onResume() { super.onResume(); loadVaultPhotos() }
    private fun loadVaultPhotos() {
        galleryContainer.removeAllViews()
        val vaultDir = File(filesDir, "intruders")
        val files = vaultDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
        if (files.isEmpty()) {
            galleryContainer.addView(TextView(this).apply { text = "Belum ada foto penyusup yang tersimpan."; setPadding(0, 24, 0, 24) })
            return
        }
        for (file in files) {
            val itemLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 24, 0, 24); gravity = Gravity.CENTER_HORIZONTAL }
            val imgView = ImageView(this).apply { setImageBitmap(BitmapFactory.decodeFile(file.absolutePath)); adjustViewBounds = true; maxHeight = 650 }
            itemLayout.addView(imgView); itemLayout.addView(TextView(this).apply { text = file.name; setPadding(0, 8, 0, 16) })
            galleryContainer.addView(itemLayout)
        }
    }
    private fun requestPermissionsAndAdmin() {
        val perms = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) perms.add(Manifest.permission.POST_NOTIFICATIONS)
        ActivityCompat.requestPermissions(this, perms.toTypedArray(), 200)
        val compName = ComponentName(this, GuardAdminReceiver::class.java)
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (!dpm.isAdminActive(compName)) {
            startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Aktifkan izin ini untuk memotret otomatis saat ada yang salah memasukkan PIN atau Pola layar kunci.")
            })
        }
    }
}
