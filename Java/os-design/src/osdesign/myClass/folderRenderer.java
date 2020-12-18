package osdesign.myClass;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * 结点渲染器，呈现结点图标
 */
public class folderRenderer extends DefaultTreeCellRenderer {

    private static FileSystemView fsView;
    private static final long serialVersionUID = 1L;

    //重写父类方法

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus) {

        //如果 expanded 为 true，则当前扩展该节点，如果 leaf 为 true，则该节点表示叶节点，如果 hasFocus 为 true，则该节点当前拥有焦点

        fsView = FileSystemView.getFileSystemView();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) value;
        nodeData data = (nodeData) selectedNode.getUserObject();
        Icon icon = fsView.getSystemIcon(data.f);
        setLeafIcon(icon);//叶节点
        setOpenIcon(icon);//扩展的非叶节点
        setClosedIcon(icon);//无扩展的非叶节点
        return super.getTreeCellRendererComponent(tree, value, selected, expanded,
                leaf, row, hasFocus);

    }

}