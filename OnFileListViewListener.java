package com.ayu.mysave.widget;

public interface OnFileListViewListener {
    //文件被点击时，参数为文件名
    public void onFileItemClick(String filename);
    //文件夹被地点击是，参数为文件夹的路径
    public void onDirItemClick(String path);
    //按返回键，返回上一层目录
    public void onBack();
}
