import os

with open('app/src/main/java/com/example/a2dgame/LevelOneFragment.kt', 'r', encoding='utf-8') as f:
    text = f.read()

# I want to completely replace the portion starting from handleBoxTap to checkGameResults
p_start = text.find('    private fun handleBoxTap(index: Int) {')
p_end = text.find('    private fun showWinDialog() {')

if p_start == -1 or p_end == -1:
    print("Could not find boundaries!")
    exit(1)

with open('patch_methods.txt', 'r', encoding='utf-8') as f:
    patch = f.read()

# patch_methods.txt starts with animateSelection and goes all the way to dismissWinDialogAndProceed.
# Wait, I don't want to replace handleBoxTap with patch, patch_methods.txt only has animateSelection onwards!
# Let's get the sophisticated handleBoxTap

sophisticated_handle_box_tap = """    private fun handleBoxTap(index: Int) {
        if (engine.isGameOver || animatingBoxes.contains(index)) return
        if (isMagnifyMode) {
            val box = engine.getBoxes().find { it.id == index }
            if (box != null && !box.isArchived && box.hiddenLayers > 0) {
                powerupMagnify--
                engine.revealHiddenLayers(index)
                soundManager?.play("complete")
            }
            isMagnifyMode = false
            updatePowerupButtons()
            renderBoard()
            return
        }

        val boxes = engine.getBoxes()
        val clickedBox = boxes.find { it.id == index } ?: return
        val selectedIdx = engine.selectedBoxIndex

        if (selectedIdx == null) {
            if (!clickedBox.isEmpty() && !clickedBox.isFrozen && !clickedBox.hasCobweb && !clickedBox.isComplete()) {
                engine.selectedBoxIndex = index
                soundManager?.play("pickup")
                val view = binding.glGameBoard.findViewWithTag<View>(index)
                animateSelection(view, true)
            }
        } else if (selectedIdx == index) {
            engine.selectedBoxIndex = null
            soundManager?.play("drop")
            val view = binding.glGameBoard.findViewWithTag<View>(index)
            animateSelection(view, false)
        } else {
            val srcBox = boxes.find { it.id == selectedIdx }!!
            if (engine.canMove(srcBox, clickedBox)) {
                animateMoveSequence(selectedIdx, index)
            } else {
                val oldView = binding.glGameBoard.findViewWithTag<View>(selectedIdx)
                animateSelection(oldView, false)
                
                if (!clickedBox.isEmpty() && !clickedBox.isFrozen && !clickedBox.hasCobweb && !clickedBox.isComplete()) {
                    engine.selectedBoxIndex = index
                    soundManager?.play("pickup")
                    val view = binding.glGameBoard.findViewWithTag<View>(index)
                    animateSelection(view, true)
                } else {
                    engine.selectedBoxIndex = null
                    soundManager?.play("drop")
                }
            }
        }
    }
"""

# Let's find where checkGameResults ends in patch so we don't mess up showWinDialog
p_cg_end = patch.find('    private fun showWinDialog() {')
if p_cg_end != -1:
    patch = patch[:p_cg_end]

# Also patch needs playBackgroundMusic? No, it's already below showWinDialog or somewhere else.
# Wait, patch_methods.txt currently contains `checkGameResults` without win dialog animations because it ends at `showWinDialog()`.
# Wait, let's assemble it.

new_text = text[:p_start] + sophisticated_handle_box_tap + "\n" + patch + text[p_end:]

with open('app/src/main/java/com/example/a2dgame/LevelOneFragment.kt', 'w', encoding='utf-8') as f:
    f.write(new_text)
print("Updated successfully!")
