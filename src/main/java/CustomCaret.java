import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;

class CustomCaret extends DefaultCaret {

    private final Color color;

    CustomCaret(Color color) {
        this.color = color;
        setBlinkRate(500); // sets the blink rate to 500 milliseconds
    }

    @Override
    public void paint(Graphics g) {
        JTextComponent comp = getComponent();
        if (comp != null) {
            int dot = getDot();
            Rectangle r;
            try {
                r = comp.modelToView(dot);
                if ((x != r.x) || (y != r.y)) {
                    // paint() has been called directly, without a previous call to
                    // damage(), so do some cleanup. (This happens, for example, when
                    // the text component is resized.)
                    repaint(); // erase previous location of caret
                    x = r.x;
                    y = r.y;
                }
            } catch (BadLocationException e) {
                r = null;
            }

            if (isVisible()) {
                try {
                    g.setColor(color);
                    // Render cursor
                    if (r != null)
                        g.fillRect(r.x, r.y, 1, r.height);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}