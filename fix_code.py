import os

with open('app/src/main/java/com/example/a2dgame/LevelOneFragment.kt', 'r', encoding='utf-8') as f:
    curr = f.read()

with open('handle.txt', 'r', encoding='utf-8') as f:
    handle = f.read()

with open('moves.txt', 'r', encoding='utf-8') as f:
    moves = f.read()

# Replace handleBoxTap
c1 = curr.find('    private fun handleBoxTap(index: Int) {')
c2 = curr.find('    private fun animateMoveSequence(srcId: Int, dstId: Int) {')
if c1 != -1 and c2 != -1:
    curr = curr[:c1] + handle + curr[c2:]

# Replace animateMoveSequence
c3 = curr.find('    private fun animateMoveSequence(srcId: Int, dstId: Int) {')
c4 = curr.find('    private fun checkGameResults() {')
if c3 != -1 and c4 != -1:
    curr = curr[:c3] + moves + curr[c4:]

# Update other simple pieces
curr = curr.replace('engine.rerollBags()\n            updatePowerupButtons()', 'engine.rerollBags()\n            engine.archiveAllReady()\n            soundManager?.play("pickup")\n            updatePowerupButtons()')
curr = curr.replace('engine.shuffleAllBoxes()\n            updatePowerupButtons()', 'engine.shuffleAllBoxes()\n            soundManager?.play("move")\n            updatePowerupButtons()')

curr = curr.replace('soundManager = SoundManager(requireContext())', 'soundManager = SoundManager(requireContext())\n        soundManager?.setEnabled(true)')

curr = curr.replace('private fun showWinDialog() {', 'private fun showWinDialog() {\n        soundManager?.playWin()')

old_lose = 'private fun showLoseDialog() {\n        soundManager?.playLose()\n        val vibrator = requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator\n        if (vibrator?.hasVibrator() == true) { vibrator.vibrate(200) }\n        val shakeAnim = android.animation.ObjectAnimator.ofFloat(binding.glGameBoard, android.view.View.TRANSLATION_X, 0f, 18f, -18f, 14f, -14f, 8f, -8f, 0f).apply { duration = 450; start() }\n        isLoseDialogShowing = true\n        binding.layoutLoseDialog.root.visibility = android.view.View.VISIBLE'
curr = curr.replace('private fun showLoseDialog() {\n        isLoseDialogShowing = true\n        binding.layoutLoseDialog.root.visibility = View.VISIBLE', old_lose)

with open('app/src/main/java/com/example/a2dgame/LevelOneFragment.kt', 'w', encoding='utf-8') as f:
    f.write(curr)
print('Patched successfully!')
