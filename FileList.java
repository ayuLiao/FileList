package com.ayu.mysave.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class FileList  extends ListView implements
        android.widget.AdapterView.OnItemClickListener{
    private final String namespace = "http://mysave.ayu.com";
    //sd卡路径
    private String sdcardDirectory;
    //文件列表
    private List<File> fileList = new ArrayList<File>();
    //Stack类是java自己实现的一个栈，先进后出
    private Stack<String> dirStack = new Stack<String>();
    //File列表的适配器
    private FileListAdapte fileListAdapte;
    //提供给外部使用的抽象接口
    private OnFileListViewListener onFileListViewListener;
    //文件夹要显示的图片ID
    private int folderImageResId;
    //其他文件要显示的图片ID
    private int otherFileImageResId;
    //Map的参数要为泛型
    private Map<String, Integer> fileImageResIdMap = new HashMap<String,Integer>();
    //判断是否为Folder
    private boolean isFolder = false;

    //继承了ListView，实现相应的构造方法
    public FileList(Context context, AttributeSet attrs) {
        super(context, attrs);
        //SD卡根目录
        sdcardDirectory = android.os.Environment.getExternalStorageDirectory().toString();
        setOnItemClickListener(this);//为每个Item设置监听事件
        setBackgroundColor(Color.WHITE);//设置背景颜色为白色
        //根据配置文件获得文件图片id，参数一为命名空间,参数二为对应标签的名，默认返回值
        folderImageResId = attrs.getAttributeResourceValue(namespace,"folderImage",0);
        otherFileImageResId = attrs.getAttributeResourceValue(namespace,"otherFileImage",0);
        isFolder = attrs.getAttributeBooleanValue(namespace,"isFolder",false);

        int index = 1;
        while (true){
            //文件扩展名
            String extName = attrs.getAttributeValue(namespace,"extName"+index);
            int fileImageResId = attrs.getAttributeResourceValue(namespace,"fileImage"+index,0);

            //配置文件中，只用两个extName扩展名，所以取完这两个扩展名，就可以跳出了
            if("".equals(extName) || extName == null || fileImageResId == 0){
                break;
            }
            //将扩展名和对应的图片绑定在Map中
            fileImageResIdMap.put(extName, fileImageResId);

            index++;

        }
        //将sd卡根目录压入栈中
        dirStack.push(sdcardDirectory);
        addFiles();//添加文件

        fileListAdapte = new FileListAdapte(getContext());
        setAdapter(fileListAdapte);
    }

    //当前目录下的所以文件都放到fileList中
    private void addFiles() {
        fileList.clear();

        String currentPath = getCurrentPath();//获得当前路径
        File[] files = new File(currentPath).listFiles();//获得当前目录中的File对象

        //大于1，表示当前不是SD卡的更目录
        if(dirStack.size() > 1){
            fileList.add(null);
        }

        for(File file : files){
            //只添加表示目录的文件
            if(isFolder){
                if(file.isDirectory()){
                    fileList.add(file);
                }
            }
            else{
                fileList.add(file);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //返回上一页
        if(fileList.get(position) == null){
            dirStack.pop();
            addFiles();
            fileListAdapte.notifyDataSetChanged();
            if(onFileListViewListener != null){
                onFileListViewListener.onDirItemClick(getCurrentPath());
            }
        }else if(fileList.get(position).isDirectory()){
            dirStack.push(fileList.get(position).getName());
            addFiles();
            fileListAdapte.notifyDataSetChanged();
            if(onFileListViewListener != null){
                onFileListViewListener.onDirItemClick(getCurrentPath());
            }
        }else{
            if(onFileListViewListener != null){
                String filename = getCurrentPath() + "/"
                        + fileList.get(position).getName();
                onFileListViewListener.onFileItemClick(filename);
            }
        }
    }

    //获得当前路径
    public String getCurrentPath() {
        String path = "";
        for(String dir : dirStack){
            path += dir + "/";
        }
        path = path.substring(0,path.length() - 1);
        return path;
    }

    private String getExtName(String filename){
        int position = filename.lastIndexOf(".");
        if(position >= 0 ){
            return filename.substring(position + 1);
        }else{
            return "";
        }
    }


    public class FileListAdapte extends BaseAdapter {

        Context context;
        public FileListAdapte(Context context){
            this.context = context;
        }

        //获得fileList中元素的个数
        @Override
        public int getCount() {
            return fileList.size();
        }

        //获得fileList中对应位置的元素
        @Override
        public Object getItem(int position) {
            return fileList.get(position);
        }

        //获得fileList中元素的位置
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout fileLayout = new LinearLayout(context);
            fileLayout.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
            ));
            fileLayout.setOrientation(LinearLayout.HORIZONTAL);
            fileLayout.setPadding(5,10,0,10);
            ImageView ivFile = new ImageView(context);
            ivFile.setLayoutParams(new LayoutParams(100,100));
            TextView tvFile = new TextView(context);
            tvFile.setTextColor(Color.BLACK);
            tvFile.setTextAppearance(context,
                    android.R.style.TextAppearance_Large);
            tvFile.setPadding(5,5,0,0);
            if(fileList.get(position) == null){
                if(folderImageResId > 0 ){
                    ivFile.setImageResource(folderImageResId);
                }
                tvFile.setText(". .");
            }else{
                tvFile.setText(fileList.get(position).getName());
                Integer resId = fileImageResIdMap.get(getExtName(fileList.get(position).getName()));
                int fileImageResId = 0;
                if(resId != null){
                    if(resId >0){
                        fileImageResId = resId;
                    }
                }
                if(fileImageResId > 0){
                    ivFile.setImageResource(fileImageResId);
                }else if(otherFileImageResId >0){
                    ivFile.setImageResource(otherFileImageResId);
                }
            }

            tvFile.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.MATCH_PARENT));
            fileLayout.addView(ivFile);
            fileLayout.addView(tvFile);
            return fileLayout;
        }
    }

    public void setOnFileListViewListener(OnFileListViewListener listener){
        this.onFileListViewListener = listener;
    }
}
