package com.example.sqlitemp3

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sqlitemp3.databinding.FragmentPlaymp3Binding

class Playmp3Fragment : Fragment() {

    private lateinit var binding: FragmentPlaymp3Binding
    private lateinit var myHelper: MyDBHelper
    private lateinit var sqlDB: SQLiteDatabase
    private lateinit var musicAdapter: MusicFileAdapter
    private var mPlayer: MediaPlayer? = null

    // raw 폴더에 있는 노래 목록
    private val mp3List = listOf(
        "eta" to R.raw.eta,
        "howsweet" to R.raw.howsweet,
        "supernatural" to R.raw.supernatural,
        "homesweethome" to R.raw.homesweethome,
    )

    private var currentSongPosition: Int = -1
    private var isPlaying: Boolean = false
    private lateinit var handler: Handler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaymp3Binding.inflate(inflater, container, false)
        myHelper = MyDBHelper(requireContext())

        handler = Handler(Looper.getMainLooper())

        setupRecyclerView()
        loadMusicData()
        setupButtonListeners()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvMusicFile.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadMusicData() {
        sqlDB = myHelper.readableDatabase
        val cursor: Cursor = sqlDB.rawQuery("SELECT * FROM groupTBL;", null)

        val musicList = mutableListOf<Music>()

        while (cursor.moveToNext()) {
            val title = cursor.getString(0)
            val name = cursor.getString(1)
            val genre = cursor.getString(2)
            val playTime = cursor.getString(3)

            musicList.add(Music(title, name, genre, playTime))
        }

        musicAdapter = MusicFileAdapter(musicList) { selectedMusic ->
            binding.tvMP3.text = "선택된 음악 : ${selectedMusic.title}"
            binding.btnPlay.isClickable = true
            binding.btnStop.isClickable = false
            currentSongPosition = musicList.indexOf(selectedMusic)
            selectedSongTitle = selectedMusic.title
        }

        binding.rvMusicFile.adapter = musicAdapter

        cursor.close()
        sqlDB.close()
    }

    private var selectedSongTitle: String? = null

    private fun setupButtonListeners() {
        binding.btnPlay.setOnClickListener {
            selectedSongTitle?.let { title ->
                val selectedMp3 = mp3List.find { it.first == title.lowercase() }
                if (selectedMp3 != null) {
                    mPlayer?.release()
                    mPlayer = MediaPlayer.create(requireContext(), selectedMp3.second)
                    mPlayer?.start()

                    binding.tvMP3.text = "실행중인 음악 : $title"
                    binding.btnPlay.isClickable = false
                    binding.btnStop.isClickable = true
                    binding.sbMP3.visibility = View.VISIBLE

                    isPlaying = true

                    // SeekBar 업데이트
                    updateSeekBar()

                    mPlayer?.setOnCompletionListener {
                        isPlaying = false
                        binding.btnPlay.isClickable = true
                        binding.btnStop.isClickable = false
                        binding.sbMP3.progress = 0

                        // 현재 곡의 위치 업데이트 및 다음 곡 재생
                        currentSongPosition = (currentSongPosition + 1) % mp3List.size // 마지막 곡이면 첫 곡으로
                        playSelectedSongAtPosition()  // 다음 곡 재생
                    }
                } else {
                    binding.tvMP3.text = "해당 음악 파일을 찾을 수 없습니다."
                }
            }
        }

        binding.btnStop.setOnClickListener {
            mPlayer?.stop()
            mPlayer?.reset()
            binding.btnPlay.isClickable = true
            binding.btnStop.isClickable = false
            binding.sbMP3.progress = 0
            binding.tvMP3.text = "노래 재생 중지"
        }

        binding.btnPause.setOnClickListener {
            if (isPlaying) {
                mPlayer?.pause()
                isPlaying = false
            } else {
                mPlayer?.start()
                isPlaying = true
            }
        }

        binding.btnRewind.setOnClickListener {
            if (currentSongPosition > 0) {
                currentSongPosition -= 1
                playSelectedSongAtPosition()
            } else {
                // 첫 곡에서 이전 버튼을 누르면 마지막 곡으로 이동
                currentSongPosition = mp3List.size - 1
                playSelectedSongAtPosition()
            }
        }

        binding.btnForward.setOnClickListener {
            if (currentSongPosition < mp3List.size - 1) {
                currentSongPosition += 1
                playSelectedSongAtPosition()
            } else {
                // 마지막 곡에서 다음 버튼을 누르면 첫 곡으로 이동
                currentSongPosition = 0
                playSelectedSongAtPosition()
            }
        }


        binding.btnRewind10.setOnClickListener {
            val newPosition = (mPlayer?.currentPosition ?: 0) - 10000
            mPlayer?.seekTo(newPosition.coerceAtLeast(0))
            // 10초 뒤로 (음악 시작점 이하로는 안 가게 설정)
            updateSeekBar()
        }

        binding.btnForward10.setOnClickListener {
            val newPosition = (mPlayer?.currentPosition ?: 0) + 10000
            mPlayer?.seekTo(newPosition.coerceAtMost(mPlayer?.duration ?: 0))
            // 10초 앞으로 (음악 길이 이상으로는 안 가게 설정)
            updateSeekBar()
        }

    }

    private fun updateSeekBar() {
        val updateRunnable = object : Runnable {
            override fun run() {
                if (mPlayer != null && mPlayer!!.isPlaying) {
                    // SeekBar 진행 상태를 현재 위치와 음악 길이에 비례해 업데이트
                    val progress = (mPlayer!!.currentPosition * 100) / mPlayer!!.duration
                    binding.sbMP3.progress = progress
                    binding.tvMP3.text = "실행중인 음악 : ${selectedSongTitle}"
                    handler.postDelayed(this, 1000)  // 1초마다 실행
                }
            }
        }
        handler.post(updateRunnable)
    }

    private fun playSelectedSongAtPosition() {
        val selectedTitle = mp3List[currentSongPosition].first
        val selectedMp3 = mp3List[currentSongPosition].second

        mPlayer?.release()
        mPlayer = MediaPlayer.create(requireContext(), selectedMp3)
        mPlayer?.start()

        binding.tvMP3.text = "실행중인 음악 : $selectedTitle"
        binding.btnPlay.isClickable = false
        binding.btnStop.isClickable = true
        binding.sbMP3.visibility = View.VISIBLE

        isPlaying = true
        updateSeekBar()

        mPlayer?.setOnCompletionListener {
            isPlaying = false
            binding.btnPlay.isClickable = true
            binding.btnStop.isClickable = false
            binding.sbMP3.progress = 0
        }

        selectedSongTitle = selectedTitle

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPlayer?.release()
        mPlayer = null
    }

    inner class MyDBHelper(context: Context) : SQLiteOpenHelper(context, "mp3DB", null, 1) {
        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(
                "CREATE TABLE groupTBL (" +
                        "gTitle CHAR(20) PRIMARY KEY, " +
                        "gName CHAR(20), " +
                        "gGenre CHAR(20), " +
                        "gPlayTime CHAR(20));"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("DROP TABLE IF EXISTS groupTBL")
            onCreate(db)
        }
    }
}
