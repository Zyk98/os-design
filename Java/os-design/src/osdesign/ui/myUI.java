package osdesign.ui;

import osdesign.CLibrary;
import osdesign.myClass.*;
import com.sun.jna.platform.win32.WinNT;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.concurrent.ForkJoinPool;

//保存、另存为、撤销

public class myUI {

    //windows内核操作handle（类似指针）
    WinNT.HANDLE handle = null;

    //初始化几个对象
    byteTo byteTo = new byteTo();//字节数转换成KB、MB、GB的封装方法
    private static ForkJoinPool forkJoinPool = null;//jdk7的fork多线程实现递归读取文件夹所有文件的大小总和
    myFileUtils mFU = new myFileUtils();//封装了一个关于文件操作的类
    //判断该执行剪切还是复制
    boolean isCurt = false;

    //用于文件的复制和粘贴操作
    String srcFilePath = null;
    String destFilePath = null;
    String srcFilePathName = null;

    TreePath tp;
    JFrame jf;
    JTree tree;//树结构
    JTable jt;//表格
    JPanel jp = new JPanel();
    Object[][] list = {{}};
    DefaultTableModel tableModel;//表格模型
    DefaultMutableTreeNode parent; //树形结构的一个节点
    DefaultTreeModel model;//声明树状视图模型  以指定的模型创建一棵树
    JPopupMenu popmenu = new JPopupMenu();//右键弹出菜单
    JMenuItem deleteItem = new JMenuItem("删除");
    JMenuItem renameItem = new JMenuItem("重命名");
    JMenuItem natureItem = new JMenuItem("属性");
    JMenuItem copyItem = new JMenuItem("复制");
    JMenuItem pasteItem = new JMenuItem("粘贴");
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(new nodeData(null, "此电脑"));//自定义一个名为root的节点，后面作为根节点创建一棵树  NodeData是自定义节点类。
    DefaultMutableTreeNode aClone;//用于重命名时生成一个原节点的克隆体

    //设置JFrame
    public void setJFrame(){
        jf = new JFrame("文件资源管理器(郑永康)");
        jf.setSize(900, 1000);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setLayout(new FlowLayout());
    }

    // 设置菜单栏
    public void setMenu(){
        JMenuBar jmb=new JMenuBar();
        JMenu jMenu1=new JMenu("文件");
        JMenu jMenu2=new JMenu("编辑");
        JMenu jMenu3=new JMenu("帮助");

        JMenuItem jMenuItem11=new JMenuItem("新建");
        jMenuItem11.addActionListener(newListener);
        JMenuItem jMenuItem12=new JMenuItem("打开");
        jMenuItem12.addActionListener(openListener);
        JMenuItem jMenuItem13=new JMenuItem("保存");
        jMenuItem13.addActionListener(saveListener);
        JMenuItem jMenuItem14=new JMenuItem("另存为");
        jMenuItem14.addActionListener(savetoListener);
        JMenuItem jMenuItem15=new JMenuItem("最近的文件");
        jMenuItem15.addActionListener(latelyListener);
        JMenuItem jMenuItem16=new JMenuItem("退出");
        jMenuItem16.addActionListener(exitListener);
        jMenu1.add(jMenuItem11);
        jMenu1.add(jMenuItem12);
        jMenu1.add(jMenuItem13);
        jMenu1.add(jMenuItem14);
        jMenu1.add(jMenuItem15);
        jMenu1.add(jMenuItem16);

        JMenuItem jMenuItem21=new JMenuItem("撤销");
        JMenuItem jMenuItem22=new JMenuItem("剪切");
        jMenuItem22.addActionListener(cutListener);
        JMenuItem jMenuItem23=new JMenuItem("粘贴");
        jMenuItem23.addActionListener(pasteListener);
        JMenuItem jMenuItem24=new JMenuItem("复制");
        jMenuItem24.addActionListener(copyListener);
        jMenu2.add(jMenuItem21);
        jMenu2.add(jMenuItem22);
        jMenu2.add(jMenuItem23);
        jMenu2.add(jMenuItem24);

        JMenuItem jMenuItem31=new JMenuItem("关于");
        jMenuItem31.addActionListener(aboutListener);
        jMenu3.add(jMenuItem31);

        jmb.add(jMenu1);
        jmb.add(jMenu2);
        jmb.add(jMenu3);
        jf.setJMenuBar(jmb);
    }

    //设置JPanel
    public void setJPanel(){
        jp.setLayout ( new  BoxLayout (jp, BoxLayout.X_AXIS));
        jp.setPreferredSize ( new  Dimension ( 900 ,  1000 ));
        jf.setContentPane (jp);
    }

    //设置JTable
    public void setJTable(){
        String[] row = {"Name", "Size","Type", "Modified"};
        tableModel = new DefaultTableModel(list, row);
        jt = new JTable(tableModel);
        jt.setRowHeight(25);
        jt.setEnabled(false);
        TableRowSorter<TableModel> sorter = new TableRowSorter(tableModel);//排序
        jt.setRowSorter(sorter);
        JScrollPane scrollTable = new JScrollPane(jt);//显示区为table表格的可滚动面板
        scrollTable.setPreferredSize(new Dimension(600, 300));
        jp.add(scrollTable);
    }

    //初始化树
    public void treeInit(){
        File[] roots = File.listRoots();//获得系统根目录文件  需要获取磁盘中所有的盘符路径

        for (int i = 0; i < roots.length; i++) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new nodeData(roots[i], roots[i].getAbsolutePath()));//NodeDate自定义节点类,file + name
            root.add(node);//从父节点删除 newChild并将其添加到该节点的子数组的末尾，使其成为该节点的子节点
        }

        tree = new JTree(root);//以指定的自定义的节点(root)作为根节点创建一棵树
        model = (DefaultTreeModel) tree.getModel();//获取JTree对应的TreeModel的对象，即获取树的数据模型
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);//一次只能选中一个节点先返回树的选择模型才能设置树的选择模型
        folderRenderer folderRenderer = new folderRenderer();
        folderRenderer.setPreferredSize(new Dimension(170,22));
        tree.setCellRenderer(folderRenderer);//设置使用定制的节点绘制器
    }

    //制作JPopupMenu
    public void setJPopupMenu(){
        //为pop添加菜单项

        popmenu.add(deleteItem); popmenu.addSeparator();
        popmenu.add(renameItem); popmenu.addSeparator();
        popmenu.add(copyItem); popmenu.addSeparator();
        popmenu.add(pasteItem); popmenu.addSeparator();
        popmenu.add(natureItem);//属性功能

        natureItem.addActionListener(natureListener);
        deleteItem.addActionListener(deleteListener);
        renameItem.addActionListener(renameListener);
        copyItem.addActionListener(copyListener);
        pasteItem.addActionListener(pasteListener);

        //向窗口添加PopupMenu对象
        jf.add(popmenu);
    }

    //对整个文件树进行初始化
    public void init() {
        setJFrame();
        setMenu();
        setJPanel();
        treeInit();
        setJTable();
        setJPopupMenu();
        jf.setVisible(true);


        //通知“节点改变”，实现一个TreeModelListener 。当用户为一个树节点输入一个新名字时，事件会被检测到。
        model.addTreeModelListener(new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e){//当树的节点改变时就调用这个方法，属于递归DFS的方法范畴
                //获得编辑后的节点的父节点
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());//返回从根节点到该节点的路径，在找到树种最后一个选中的节点
                DefaultMutableTreeNode node;
                try {
                    int[] index = e.getChildIndices();//返回目前修改点的索引值
                    node = (DefaultMutableTreeNode) (parent.getChildAt(index[0]));//getChildAt()方法取得修改的节点对象
                    // 克隆体更新名称，file不变
                    ((nodeData) aClone.getUserObject()).changeString(node.toString());
                    //删除选定节点且要求该节点存在父节点
                    model.removeNodeFromParent(node);
                    //添加克隆体
                    model.insertNodeInto(aClone, parent, index[0]);//在父节点的子节点中的 index 处插入aClone
                } catch (NullPointerException exc){ //點選的節點為root node,則getChildIndices()的返回值為null
                    System.out.println("model error");
                }

                //系统实现改名
                nodeData data = (nodeData) aClone.getUserObject();
                String tt = data.f.getParent() + "//";
                tt = tt + aClone.toString();
                File newfile = new File(tt);
                data.f.renameTo(newfile);//将文件改名为 指定的名字
                data.f = newfile;
                return;
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
            }   //当树的结构改变时就调用这个方法

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
            }  // 当树的节点删除时就调用这个方法

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
            } //当树的节点添加时就调用这个方法

        });



        //节点的鼠标事件监视器
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e)//单右键单击时，也选中
            {
                TreePath tp = tree.getPathForLocation(e.getX(), e.getY()); //返回指定节点的完整的信息
                if (tp == null)
                    return;
                tree.setSelectionPath(tp); //选中节点选择指定路径标识的节点
                // 如果是右键点击，则不必考虑展开
                if (SwingUtilities.isRightMouseButton(e)){
//                    popmenu.show(e.getComponent(),e.getX(),e.getY());
                    return;
                }


                //如果是左键点击，就展开或者合上
                if (tree.isExpanded(tp))//由Path所确定的节点被展开，则返回true
                {
                    tree.collapsePath(tp);//将Path所确定的节点收缩，并保证可见
                } else {
                    tree.expandPath(tp);   //将Path所确定的节点展开，并保证可见
                }
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2)//如果连击
                {
                    TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
                    if (tp != null) {
                        DefaultMutableTreeNode temp = (DefaultMutableTreeNode) tp.getLastPathComponent();//获得选中节点
                        if (temp == null)
                            return;

                        //如果节点名被改变了，节点数据自动被改成String型的，而不再是NodeData型的
                        if (temp.getUserObject() == temp.getUserObject().toString()) {
                            System.out.println("Object of getUserObject() has been changed");
                            return;
                        }

                        nodeData data = (nodeData) temp.getUserObject();//获得数据节点
                        if (tp != null && data.f.isFile())//如果是可执行文件
                        {
                            try {
                                //执行选中文件，调用具体命令行功能的程序,根据命令行格式加上文件名
                                Runtime ce = Runtime.getRuntime();
                                ce.exec("cmd /c start " + data.f.toString());
                            } catch (Exception e1) {
                                System.out.println(e1);
                            }
                        }
                    }
                }
            }

            public void mouseReleased(MouseEvent e)//鼠标松开时，如果是右击，则显示右击菜单
            {
                if (e.isPopupTrigger())//测试是否这个事件将引起一个弹出式菜单在平台中探出
                {
                    TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
                    //如果右击节点
                    if (tp == null)
                        return;
                    popmenu.show(tree, e.getX(), e.getY());
                }
            }
        };
        tree.addMouseListener(ml);


        //节点选中事件监视器   事件监听器作为匿名内部类
        tree.addTreeSelectionListener(new TreeSelectionListener(){//节点选中事件监视器 添加 TreeSelection事件的监听器。
            //重写接口方法valueChanged  每当选择的值更改时调用
            public void valueChanged(TreeSelectionEvent e) {
                TreePath movepath = (TreePath) e.getNewLeadSelectionPath();//返回当前前导路径
                // 如果节点被菜单选项deleteItem删除了,就会返回null,此时什么都不做，返回即可
                if (movepath == null)
                    return;
                DefaultMutableTreeNode temp = (DefaultMutableTreeNode) movepath.getLastPathComponent();//获得选中节点
                if (temp == null)
                    return;


                //如果节点名被改变了，节点数据自动被改成String型的，而不再是NodeData型的
                if (temp.getUserObject() == temp.getUserObject().toString()) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    //测试是否进来
                    System.out.println(aClone.toString());
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
                    int selectedIndex = parent.getIndex(selectedNode);
                    System.out.println("Object of getUserObject() has been changed");
                    //克隆体更新名称，file不变
                    ((nodeData) aClone.getUserObject()).changeString(selectedNode.toString());
                    //删除选定节点
                    model.removeNodeFromParent(selectedNode);
                    //添加克隆体
                    model.insertNodeInto(aClone, parent, selectedIndex + 1);
                    //系统实现改名
                    nodeData data = (nodeData) aClone.getUserObject();
                    String tt = data.f.getParent() + "//";
                    tt = tt + aClone.toString();
                    data.f.renameTo(new File(tt));
                    //设置选中节点，避免重复触发该控制流
                    tree.setSelectionRow(selectedIndex + 1);
                    return;

                }

                nodeData data = (nodeData) temp.getUserObject();//获得数据节点
                if (data.f != null) {
                    //处理初次选中后快速添加新子节点代表新的文件
                    if (data.f.isDirectory() && temp.isLeaf()){//如果是目录，但目前还是叶节点，那么就添加
                        File[] RRoots = data.f.listFiles();
                        for (int j = 0; j < RRoots.length; j++) {
                            DefaultMutableTreeNode NNode = new DefaultMutableTreeNode(new nodeData(RRoots[j], RRoots[j].getName()));
                            model.insertNodeInto(NNode, temp, temp.getChildCount());//添加新节点并自动刷新
                        }

                        //实时表格构造
                        tableModel.setRowCount(0);
                        list = initTable(RRoots);//在fu方法中进行表格list的赋值
                        for (int i = 0; i < RRoots.length; i++) {
                            tableModel.addRow(list[i]);
                        }

                    } else if (data.f.isFile()){//如果是文件，什么都不做
                    }
                } else {
                    System.out.println("无法获得选中的节点");
                }
            }
        });

        JScrollPane scrollTree = new JScrollPane(tree);//显示区为table表格的可滚动面板
        scrollTree.setPreferredSize(new Dimension(400, 300));
        jp.add(scrollTree);

    }


    //读取所选节点,获取子节点
    public Object[][] initTable(File[] file) {
        Object[][] m = new Object[file.length][4];
        //0代表文件名，1代表大小，2代表类型，3代表最后修改时间
        for (int i = 0; i < file.length; i++) {
            m[i][0] = file[i].getName();
            if (file[i].isDirectory()) {
                m[i][2] = "文件夹";
            } else {
                String fileName = file[i].getName();
                String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                m[i][2] = fileType;

            }
            m[i][3] = mFU.lastTime(file[i]);

            //文件大小
            try {
                forkJoinPool = new ForkJoinPool();
                long size = forkJoinPool.invoke(new directorySize.FileSizeFinder(file[i]));
                m[i][1] = byteTo.fileSizeByteToM(size);
            } catch (Exception e) {
                // TODO 自动生成 catch 块
                e.printStackTrace();
            }
        }
        return m;
    }


    //pasteItem的动作监听器
    ActionListener pasteListener = (new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
                TreePath tp = tree.getSelectionPath();//得到树状视图的被选择节点路径
                String fullPath = "";//被选择节点对应文件的完整路径信息
                for (Object obj : tp.getPath()) { //增强for循坏，每一个getPath的返回值是一个obj
                    String str = obj.toString(); //把获取的路径转成字符串
                    if (str.endsWith("\\"))//处理盘符根目录问题
                        str = str.substring(0, str.length() - 1);
                    if (fullPath.equals(""))
                        fullPath += str;
                    else
                        fullPath += "\\" + str;
                }
                int n = fullPath.indexOf("脑");
                //文件的完整路径
                String sp = fullPath.substring(n + 2);
                destFilePath = sp;

                File file = new File(srcFilePath);
                if(file.isFile()){
                    if(isCurt){
                        mFU.moveFile(srcFilePath,destFilePath+"\\"+srcFilePathName);
                    }else{
                        mFU.copyFile(srcFilePath,destFilePath+"\\"+srcFilePathName);
                    }
                }else if(file.isDirectory()){
                    mFU.copyFolder(srcFilePath,destFilePath+"\\"+srcFilePathName);
                    if(isCurt) {
                        mFU.deleteFolder(srcFilePath);
                    }
                }
        }
    });

    //copyItem的动作监听器
    ActionListener copyListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //设置可编辑
            isCurt = false;
            tree.setEditable(true);
            TreePath tp = tree.getSelectionPath();//得到树状视图的被选择节点路径
            String s1 = tp.toString();
            s1 = s1.substring(s1.lastIndexOf(","));
            s1 = s1.substring(2,s1.length()-1);
            srcFilePathName = s1;
            String fullPath = "";//被选择节点对应文件的完整路径信息
            for (Object obj : tp.getPath()) { //增强for循坏，每一个getPath的返回值是一个obj
                String str = obj.toString(); //把获取的路径转成字符串
                if (str.endsWith("\\"))//处理盘符根目录问题
                    str = str.substring(0, str.length() - 1);
                if (fullPath.equals(""))
                    fullPath += str;
                else
                    fullPath += "\\" + str;
            }
            int n = fullPath.indexOf("脑");
            //文件的完整路径
            String sp = fullPath.substring(n + 2);
            srcFilePath = sp;
        }

    };

    //cutItem的动作监听器
    ActionListener cutListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //设置可编辑
            isCurt = true;
            tree.setEditable(true);
            TreePath tp = tree.getSelectionPath();//得到树状视图的被选择节点路径
            String s1 = tp.toString();
            s1 = s1.substring(s1.lastIndexOf(","));
            s1 = s1.substring(2,s1.length()-1);
            srcFilePathName = s1;
            String fullPath = "";//被选择节点对应文件的完整路径信息
            for (Object obj : tp.getPath()) { //增强for循坏，每一个getPath的返回值是一个obj
                String str = obj.toString(); //把获取的路径转成字符串
                if (str.endsWith("\\"))//处理盘符根目录问题
                    str = str.substring(0, str.length() - 1);
                if (fullPath.equals(""))
                    fullPath += str;
                else
                    fullPath += "\\" + str;
            }
            int n = fullPath.indexOf("脑");
            //文件的完整路径
            String sp = fullPath.substring(n + 2);
            srcFilePath = sp;
        }

    };

    //renameItem的动作监控器
    ActionListener renameListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {

            //设置可编辑
            tree.setEditable(true);
            //获取选中节点
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            TreePath editPath = tree.getSelectionPath();//得到被选择节点的路径
            if (selectedNode == null)
                return;
            //开始编辑
            tree.startEditingAtPath(editPath);//选择路径中的最后一个项并试着编辑它
            // 修改节点监控器，保存节点新名字
            aClone = (DefaultMutableTreeNode) selectedNode.clone();
        }
    };

    //deleteItem的动作侦听器
    ActionListener deleteListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            //获取选中节点
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode == null)
                return;
            //获得节点数据
            nodeData data = (nodeData) selectedNode.getUserObject();
            //显示是否删除的确认对话框
            int n = JOptionPane.showConfirmDialog(tree, "是否删除?", "确认", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION)
                return;
            //如果该节点是磁盘
            if (selectedNode == root) {
                JOptionPane.showMessageDialog(tree, "本地磁盘不能被删除", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            } else if (data.f.isFile()) {
                //删除文件
                mFU.deleteFile(data.f.getAbsolutePath());
//                int temp = CLibrary.INSTANCE.DeleteFileA(data.f.getAbsolutePath());
            } else if (data.f.isDirectory()) {
                //删除文件夹
                if (selectedNode.getParent() != root)
                    mFU.deleteFolder(data.f.getAbsolutePath());
                else {
                    JOptionPane.showMessageDialog(tree, "本地磁盘不能被删除", "警告对话框", JOptionPane.WARNING_MESSAGE);
                    return;
                }

            }

            //处理树节点的删除
            model.removeNodeFromParent(selectedNode);
        }
    };

    //natureItem的动作侦听器
    ActionListener natureListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {

            JDialog subDialog;//对话框组件，用于显示文件属性信息
            subDialog = new JDialog(jf, "属性");
            subDialog.setVisible(true);
            subDialog.setSize(500, 400);
            JTextArea text = new JTextArea();
            subDialog.getContentPane().add(text);

            TreePath tp = tree.getSelectionPath();//得到树状视图的被选择节点路径
            String fullPath = "";//被选择节点对应文件的完整路径信息
            for (Object obj : tp.getPath()) { //增强for循坏，每一个getPath的返回值是一个obj
                String str = obj.toString(); //把获取的路径转成字符串
                if (str.endsWith("\\"))//处理盘符根目录问题
                    str = str.substring(0, str.length() - 1);
                if (fullPath.equals(""))
                    fullPath += str;
                else
                    fullPath += "\\" + str;
            }

            int n = fullPath.indexOf("脑");
            //文件的完整路径
            String sp = fullPath.substring(n + 2);
            File currentFile = new File(sp);
            StringBuffer sb = new StringBuffer();
            if (currentFile.isDirectory()){
                sb.append("文件夹路径：" + currentFile.getAbsolutePath() + "\n");
                sb.append("\n");
                sb.append("文件类型：文件夹\n");
                sb.append("\n");
            }
            else{
                sb.append("文件路径：" + currentFile.getAbsolutePath() + "\n");
                sb.append("\n");
                String name = currentFile.getName();
                String type = name.substring(name.lastIndexOf(".") + 1);
                sb.append("文件类型：" + type + "\n");
                sb.append("\n");
            }
            if (currentFile.canExecute()){
                sb.append("是否可执行：可执行\n");
                sb.append("\n");
            }
            else{
                sb.append("是否可执行：不可执行\n");
                sb.append("\n");
            }
            sb.append("是否可读：" + currentFile.canRead() + "\n");
            sb.append("\n");
            sb.append("是否可写：" + currentFile.canWrite() + "\n");
            sb.append("\n");
            //不是文件夹，则给出文件长度，以M结尾
            if (!currentFile.isDirectory()){
                sb.append("文件的长度：" + currentFile.length() / (1024 * 1024) + "M\n");
                sb.append("\n");
            }
            SimpleDateFormat s = new SimpleDateFormat("yyyy年MM年dd日HH小时mm分钟ss秒");
            sb.append("文件上次修改的时间：" + mFU.lastTime(currentFile) + "\n");
            sb.append("\n");
            sb.append("文件是否被隐藏：" + currentFile.isHidden() + "\n");
            sb.append("\n");
            text.setText(sb.toString());
        }
    };

    //new的动作侦听器,新建
    ActionListener newListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            TreePath tp = tree.getSelectionPath();//得到树状视图的被选择节点路径
            String fullPath = "";//被选择节点对应文件的完整路径信息
            for (Object obj : tp.getPath()) { //增强for循坏，每一个getPath的返回值是一个obj
                String str = obj.toString(); //把获取的路径转成字符串
                if (str.endsWith("\\"))//处理盘符根目录问题
                    str = str.substring(0, str.length() - 1);
                if (fullPath.equals(""))
                    fullPath += str;
                else
                    fullPath += "\\" + str;
            }

            int n = fullPath.indexOf("脑");
            //文件的完整路径
            String sp = fullPath.substring(n + 2);
            sp +="\\"+"New.txt";
            mFU.newFile(sp);
        }
    };

    //open的动作侦听器,打开
    ActionListener openListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            tp = tree.getSelectionPath();
            if (tp != null) {
                DefaultMutableTreeNode temp = (DefaultMutableTreeNode) tp.getLastPathComponent();//获得选中节点
                if (temp == null)
                    return;

                //如果节点名被改变了，节点数据自动被改成String型的，而不再是NodeData型的
                if (temp.getUserObject() == temp.getUserObject().toString()) {
                    System.out.println("Object of getUserObject() has been changed");
                    return;
                }

                nodeData data = (nodeData) temp.getUserObject();//获得数据节点
                if (tp != null && data.f.isFile())//如果是可执行文件
                {
                    try {
                        //执行选中文件，调用具体命令行功能的程序,根据命令行格式加上文件名
                        Runtime ce = Runtime.getRuntime();
                        ce.exec("cmd /c start " + data.f.toString());
                    } catch (Exception e1) {
                        System.out.println(e1);
                    }
                }
            }
        }
    };

    //save的动作侦听器,保存
    ActionListener saveListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            CLibrary.INSTANCE2.save();
//            JFileChooser fd = new JFileChooser();
//            fd.setDialogTitle("保存");
//            fd.setApproveButtonText("保存");
//            fd.showOpenDialog(null);
//            File f = fd.getSelectedFile();
//            if(f != null){}
        }
    };

    //saveto的动作侦听器,保存
    ActionListener savetoListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser jf = new JFileChooser();
            jf.setDialogTitle("另存为");
            jf.setApproveButtonText("另存为");
            jf.setFileSelectionMode(JFileChooser.SAVE_DIALOG | JFileChooser.DIRECTORIES_ONLY);
            jf.showDialog(null,null);
            File fi = jf.getSelectedFile();
            String f = fi.getAbsolutePath()+"\\test.txt";
            System.out.println("save: "+f);
            try{
                FileWriter out = new FileWriter(f);
                out.write("完成");
                out.close();
            }
            catch(Exception ex){

            }
        }
    };
    //lately的动作侦听器
    ActionListener latelyListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                //执行选中文件，调用具体命令行功能的程序,根据命令行格式加上文件名
                Runtime ce = Runtime.getRuntime();
                ce.exec("cmd /c start C:\\Users\\yongkangZheng\\Recent");
            } catch (Exception e1) {
                System.out.println(e1);
            }
        }
    };
    //关于的动作侦听器
    ActionListener aboutListener = new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent event) {
            CLibrary.INSTANCE2.box();
        }
    };
    //exit的动作侦听器,关闭
    ActionListener exitListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            System.exit(0);
        }
    };

    public static void main(String args[]) {
        new myUI().init();
    }

}