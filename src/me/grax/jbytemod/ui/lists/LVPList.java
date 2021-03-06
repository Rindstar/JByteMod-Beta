package me.grax.jbytemod.ui.lists;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import me.grax.jbytemod.utils.ErrorDisplay;
import me.grax.jbytemod.utils.dialogue.EditDialogueSpec;
import me.grax.jbytemod.utils.list.LVPEntry;
import me.grax.jbytemod.utils.list.TCBEntry;

public class LVPList extends JList<LVPEntry> {

  public void addNodes(ClassNode cn, MethodNode mn) {
    DefaultListModel<LVPEntry> model = new DefaultListModel<>();
    this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    if (mn.localVariables != null)
      for (LocalVariableNode lvn : mn.localVariables) {
        model.addElement(new LVPEntry(cn, mn, lvn));
      }
    this.setModel(model);
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          LVPEntry selected = LVPList.this.getSelectedValue();
          JPopupMenu menu = new JPopupMenu();
          if (selected != null) {
            JMenuItem remove = new JMenuItem("Remove");
            remove.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                ClassNode cn = selected.getCn();
                MethodNode mn = selected.getMn();
                mn.localVariables.remove(selected.getLvn());
                LVPList.this.addNodes(cn, mn);
              }
            });
            menu.add(remove);
            JMenuItem edit = new JMenuItem("Edit");
            edit.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                try {
                  EditDialogueSpec.createEditDialogue(mn, selected.getLvn());
                } catch (Exception ex) {
                  new ErrorDisplay(ex);
                }
                LVPList.this.addNodes(cn, mn);
              }
            });
            menu.add(edit);
          }
          JMenuItem insert = new JMenuItem("Insert");
          insert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              try {
                LocalVariableNode lvn = new LocalVariableNode("", "", "", null, null, mn.localVariables.size());
                if (EditDialogueSpec.createEditDialogue(mn, lvn))
                  if (lvn.start != null && lvn.end != null) {
                    mn.localVariables.add(lvn);
                  }
              } catch (Exception ex) {
                new ErrorDisplay(ex);
              }
              LVPList.this.addNodes(cn, mn);
            }
          });
          menu.add(insert);
          menu.show(LVPList.this, e.getX(), e.getY());
        }
      }
    });
  }
}
