import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sqlitemp3.Music
import com.example.sqlitemp3.MusicFileAdapter
import com.example.sqlitemp3.databinding.FragmentMusicFileBinding

class MusicFileFragment : Fragment() {
    private lateinit var binding: FragmentMusicFileBinding // ViewBinding 객체
    private lateinit var myHelper: myDBHelper // DB Helper 클래스
    private lateinit var sqlDB: SQLiteDatabase // SQLite 쿼리 실행 클래스
    private lateinit var musicAdapter: MusicFileAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMusicFileBinding.inflate(inflater, container, false)

        myHelper = myDBHelper(requireContext())

        binding.rvMusicFile.layoutManager = LinearLayoutManager(requireContext())

        // 초기화 버튼
        binding.btnInit.setOnClickListener {
            sqlDB = myHelper.writableDatabase
            myHelper.onUpgrade(sqlDB, 1, 2) // DB 초기화
            Toast.makeText(context, "노래 목록 초기화", Toast.LENGTH_SHORT).show()
            binding.btnSelect.callOnClick()
            sqlDB.close()
        }

        // 입력 버튼
        binding.btnInsert.setOnClickListener {
            sqlDB = myHelper.writableDatabase
            val title = binding.edtTitle.text.toString()
            val name = binding.edtName.text.toString()
            val genre = binding.edtGenre.text.toString()
            val playTime = binding.edtPlayTime.text.toString()

            if (title.isNotEmpty() && name.isNotEmpty() && genre.isNotEmpty() && playTime.isNotEmpty()) {
                sqlDB.execSQL(
                    "INSERT INTO groupTBL (gTitle, gName, gGenre, gPlayTime) VALUES (?, ?, ?, ?)",
                    arrayOf(title, name, genre, playTime)
                )
                Toast.makeText(context, "추가됨", Toast.LENGTH_SHORT).show()
                binding.edtTitle.setText("")
                binding.edtName.setText("")
                binding.edtGenre.setText("")
                binding.edtPlayTime.setText("")

                binding.edtTitle.clearFocus()
                binding.edtName.clearFocus()
                binding.edtGenre.clearFocus()
                binding.edtPlayTime.clearFocus()

                binding.btnSelect.callOnClick() // 조회 버튼 클릭
            } else {
                Toast.makeText(context, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show()
            }
            sqlDB.close()
        }

        // 수정 버튼
        binding.btnUpdate.setOnClickListener {
            sqlDB = myHelper.writableDatabase
            val title = binding.edtTitle.text.toString()
            val name = binding.edtName.text.toString()
            val genre = binding.edtGenre.text.toString()
            val playTime = binding.edtPlayTime.text.toString()

            if (title.isNotEmpty() && name.isNotEmpty() && genre.isNotEmpty() && playTime.isNotEmpty()) {
                sqlDB.execSQL(
                    "UPDATE groupTBL SET gName = ?, gGenre = ?, gPlayTime = ? WHERE gTitle = ?",
                    arrayOf(name, genre, playTime, title)
                )
                binding.edtTitle.setText("")
                binding.edtName.setText("")
                binding.edtGenre.setText("")
                binding.edtPlayTime.setText("")

                binding.edtTitle.clearFocus()
                binding.edtName.clearFocus()
                binding.edtGenre.clearFocus()
                binding.edtPlayTime.clearFocus()

                Toast.makeText(context, "수정됨", Toast.LENGTH_SHORT).show()
                binding.btnSelect.callOnClick() // 조회 버튼 클릭
            } else {
                Toast.makeText(context, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show()
            }
            sqlDB.close()
        }

        // 삭제 버튼
        binding.btnDelete.setOnClickListener {
            sqlDB = myHelper.writableDatabase
            val name = binding.edtName.text.toString()

            if (name.isNotEmpty()) {
                sqlDB.execSQL("DELETE FROM groupTBL WHERE gName = ?", arrayOf(name))

                binding.edtTitle.setText("")
                binding.edtName.setText("")
                binding.edtGenre.setText("")
                binding.edtPlayTime.setText("")

                binding.edtTitle.clearFocus()
                binding.edtName.clearFocus()
                binding.edtGenre.clearFocus()
                binding.edtPlayTime.clearFocus()

                Toast.makeText(context, "삭제됨", Toast.LENGTH_SHORT).show()
                binding.btnSelect.callOnClick() // 조회 버튼 클릭
            } else {
                Toast.makeText(context, "삭제할 가수명을 입력하세요", Toast.LENGTH_SHORT).show()
            }
            sqlDB.close()
        }

        // 조회 버튼
        binding.btnSelect.setOnClickListener {
            sqlDB = myHelper.writableDatabase
            val cursor: Cursor = sqlDB.rawQuery("SELECT * FROM groupTBL;", null)

            val musicList = mutableListOf<Music>()

            // cursor를 통해 데이터 조회
            while (cursor.moveToNext()) {
                val title = cursor.getString(0)
                val name = cursor.getString(1)
                val genre = cursor.getString(2)
                val playTime = cursor.getString(3)

                musicList.add(Music(title, name, genre, playTime))
            }

            // 어댑터에 onItemSelected 콜백 전달
            musicAdapter = MusicFileAdapter(musicList) { music ->
                binding.edtTitle.setText(music.title)
                binding.edtName.setText(music.name)
                binding.edtGenre.setText(music.genre)
                binding.edtPlayTime.setText(music.playTime)
            }

            binding.rvMusicFile.adapter = musicAdapter

            cursor.close()
            sqlDB.close()
        }

        return binding.root
    }

    // DB Helper
    inner class myDBHelper(context: Context) : SQLiteOpenHelper(context, "mp3DB", null, 1) {
        override fun onCreate(p0: SQLiteDatabase?) {
            p0!!.execSQL(
                "CREATE TABLE groupTBL (" +
                        "gTitle CHAR(20) PRIMARY KEY, " +
                        "gName CHAR(20), " +
                        "gGenre CHAR(20), " +
                        "gPlayTime CHAR(20));"
            )
        }

        override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
            p0!!.execSQL("DROP TABLE IF EXISTS groupTBL")
            onCreate(p0)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.btnSelect.callOnClick()
    }
}

