package com.example.sqlitemp3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sqlitemp3.databinding.ItemMusicFileBinding

class MusicFileAdapter(
    private val musicList: List<Music>,
    private val onItemSelected: (Music) -> Unit
) : RecyclerView.Adapter<MusicFileAdapter.MusicViewHolder>() {

    private var selectedPosition = -1  // 선택된 항목을 추적

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMusicFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MusicViewHolder(binding, onItemSelected)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        holder.bind(music, position == selectedPosition)
    }

    override fun getItemCount(): Int = musicList.size

    inner class MusicViewHolder(
        val binding: ItemMusicFileBinding,
        private val onItemSelected: (Music) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(music: Music, isSelected: Boolean) {
            binding.tvTitle.text = "노래제목 : ${music.title}"
            binding.tvName.text = "가수 : ${music.name}"
            binding.tvGenre.text = "장르 : ${music.genre}"
            binding.tvPlayTime.text = "재생 시간 : ${music.playTime}"

            // 라디오 버튼 상태 설정
            binding.rdBtn.isChecked = isSelected

            // 라디오 버튼 클릭 리스너
            binding.rdBtn.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemSelected(music)
            }
        }
    }
}

data class Music(val title: String, val name: String, val genre: String, val playTime: String)
