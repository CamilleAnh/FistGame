package com.example.a2dgame

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.a2dgame.databinding.FragmentLevelOneBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.math.ceil

class LevelOneFragment : Fragment() {

    private var _binding: FragmentLevelOneBinding? = null
    private val binding get() = _binding!!
    
    private val args: LevelOneFragmentArgs by navArgs()
    private lateinit var engine: LevelOneEngine

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLevelOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val levelId = args.levelId
        engine = LevelOneEngine(levelId)
        binding.tvLevelName.text = getString(R.string.level_name_format, levelId)
        
        loadBannerAd()
        renderBoard()

        binding.btnReset.setOnClickListener {
            engine = LevelOneEngine(levelId)
            renderBoard()
        }

        binding.btnBackLevelSelect.setOnClickListener {
            findNavController().popBackStack(R.id.SecondFragment, false)
        }

        binding.btnNextLevel.setOnClickListener {
            val nextLevelId = levelId + 1
            saveHighestLevel(nextLevelId)
            val bundle = Bundle().apply { putInt("levelId", nextLevelId) }
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.LevelOneFragment, true).build()
            findNavController().navigate(R.id.action_LevelOneFragment_self, bundle, navOptions)
        }
    }

    private fun loadBannerAd() {
        val adView = AdView(requireContext())
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        adView.setAdSize(AdSize.BANNER)
        binding.adContainer.removeAllViews()
        binding.adContainer.addView(adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun saveHighestLevel(level: Int) {
        val prefs = requireContext().getSharedPreferences("game_prefs", 0)
        val currentHighest = prefs.getInt("highest_level", 1)
        if (level > currentHighest) prefs.edit().putInt("highest_level", level).apply()
    }

    private fun renderBoard() {
        binding.glGameBoard.removeAllViews()
        val tubes = engine.getTubes()
        val activeTubes = tubes.filter { !it.isArchived }
        if (activeTubes.isEmpty()) return

        val tubeCount = activeTubes.size
        
        // 1. Xác định số cột
        val cols = when {
            tubeCount <= 12 -> 4
            tubeCount <= 20 -> 5
            tubeCount <= 30 -> 6
            else -> 7
        }
        binding.glGameBoard.columnCount = cols
        
        // 2. Xác định số hàng
        val rows = ceil(tubeCount.toDouble() / cols).toInt()

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        
        // 3. Tính chiều rộng ống
        val horizontalMargin = (16 * displayMetrics.density).toInt()
        val tubeWidth = (screenWidth - horizontalMargin - (cols * 6)) / cols
        
        // 4. Tối ưu chiều cao block dựa trên cả số cột và số hàng để tránh bị che
        var blockHeightRatio = when {
            cols <= 4 -> 0.52
            cols == 5 -> 0.48
            else -> 0.42
        }
        
        // Nếu có nhiều hàng (như trong ảnh là 2 hàng), thu nhỏ thêm để vừa màn hình
        if (rows >= 2) {
            blockHeightRatio *= 0.85 
        }

        val blockHeight = (tubeWidth * blockHeightRatio).toInt()
        val maxCapacity = activeTubes.maxOfOrNull { it.capacity } ?: 4
        val tubeHeight = (blockHeight * maxCapacity) + (10 * displayMetrics.density).toInt()

        val isBagEnabled = engine.isBagMechanismEnabled
        binding.llBoxes.isVisible = isBagEnabled
        binding.tvPackedProgress.text = engine.getProgressText()
        
        if (isBagEnabled) {
            val boxSlots = engine.getBoxSlots()
            boxSlots.forEachIndexed { i, box ->
                val tv = if (i == 0) binding.tvBoxA else binding.tvBoxB
                tv.isVisible = true
                updateBoxUI(tv, box)
            }
            if (boxSlots.size < 2) binding.tvBoxB.isVisible = false
            if (boxSlots.isEmpty()) binding.tvBoxA.isVisible = false
        }

        activeTubes.forEach { tube ->
            val index = tube.id
            val tubeContainer = FrameLayout(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = tubeWidth
                    height = tubeHeight
                    setMargins(3, 4, 3, 4)
                }
            }

            val tubeLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                
                val border = GradientDrawable().apply {
                    setColor(Color.TRANSPARENT)
                    val strokeWidth = if (cols > 5 || rows >= 2) 3 else 5
                    setStroke(strokeWidth, if (engine.selectedTubeIndex == index) Color.YELLOW else Color.parseColor("#3E2723"))
                    cornerRadius = 6f
                }
                background = border
                setPadding(2, 2, 2, 2)
                
                setOnClickListener {
                    if (engine.handleTubeClick(index)) {
                        renderBoard()
                        if (engine.isGameOver) {
                            if (engine.isWin) {
                                saveHighestLevel(args.levelId + 1)
                                Toast.makeText(context, getString(R.string.game_complete_toast), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, getString(R.string.game_over_toast), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

            tube.blocks.forEachIndexed { blockIdx, fruitColor ->
                val blockFrame = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, blockHeight).apply { setMargins(0, 1, 0, 1) }
                    background = ContextCompat.getDrawable(context, R.drawable.crate_bg)
                }
                
                val isHidden = blockIdx < tube.hiddenLayers
                if (isHidden) {
                    val tvHint = TextView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                        text = "?"
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        textSize = if (cols > 5 || rows >= 2) 11f else 15f
                        setTypeface(null, Typeface.BOLD)
                        setBackgroundColor(Color.parseColor("#80000000"))
                    }
                    blockFrame.addView(tvHint)
                } else {
                    val tvFruit = TextView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                        text = fruitColor.fruitIcon
                        gravity = Gravity.CENTER
                        textSize = if (cols > 5 || rows >= 2) 15f else 21f
                    }
                    blockFrame.addView(tvFruit)
                }
                tubeLayout.addView(blockFrame, 0)
            }
            tubeContainer.addView(tubeLayout)

            if (tube.isFrozen) {
                val ice = View(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#88ADF4FF"))
                        cornerRadius = 6f
                        setStroke(if (cols > 5) 2 else 3, Color.CYAN)
                    }
                }
                tubeContainer.addView(ice)
            }
            if (tube.hasCobweb) {
                val spider = TextView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    text = "🕸️"
                    textSize = if (cols > 5 || rows >= 2) 16f else 26f
                    gravity = Gravity.CENTER
                }
                tubeContainer.addView(spider)
            }
            binding.glGameBoard.addView(tubeContainer)
        }
        updateStatusUI()
    }

    private fun updateBoxUI(textView: TextView, box: LevelOneEngine.BoxSlot) {
        textView.text = "${box.targetColor.fruitIcon} ${box.filled}/${box.capacity} [⏳${box.turnsLeft}]"
        textView.background = GradientDrawable().apply {
            setColor(Color.parseColor("#8D6E63"))
            setStroke(3, if (box.turnsLeft <= 5) Color.RED else Color.parseColor("#FFD54F"))
            cornerRadius = 10f
        }
        textView.setTextColor(Color.WHITE)
        textView.setPadding(5, 5, 5, 5)
    }

    private fun updateStatusUI() {
        if (engine.isGameOver) {
            if (engine.isWin) {
                binding.tvInstruction.text = "CHÚC MỪNG! THU HOẠCH XONG MÀN ${engine.levelId}"
                binding.tvInstruction.setTextColor(Color.GREEN)
                binding.btnNextLevel.isVisible = true 
            } else {
                binding.tvInstruction.text = "HẾT THỜI GIAN THU HOẠCH!"
                binding.tvInstruction.setTextColor(Color.RED)
                binding.btnNextLevel.isVisible = false
            }
        } else {
            binding.tvInstruction.text = when {
                engine.levelId >= 120 -> "Phá băng bằng cách đổ trái cây cùng loại vào!"
                engine.levelId >= 80 -> "Dọn mạng nhện để thấy trái cây bên trong!"
                engine.levelId >= 20 -> "Mở các thùng bí ẩn bằng cách thu hoạch thùng phía trên!"
                else -> "Phân loại trái cây vào các thùng gỗ cùng loại."
            }
            binding.tvInstruction.setTextColor(Color.WHITE)
            binding.btnNextLevel.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
