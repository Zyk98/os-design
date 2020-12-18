// pch.cpp: 与预编译标头对应的源文件

#include "pch.h"

// 当使用预编译的头时，需要使用此源文件，编译才能成功。
#include <windows.h>
#include <tchar.h>
#include <iostream>
#include <commdlg.h>
#include <strsafe.h>
#include <string>
#include <shlwapi.h>
#include <atlstr.h>
#pragma comment(lib,"Shlwapi.lib")
using namespace std;

//消息窗
extern "C" __declspec(dllexport) void box();
void box(){
    MessageBox(NULL,_T("本文件资源管理器 FROM\n\n郑永康、杨培森、吴凡\n\n指导老师：李佳静"),_T("OS课设"), 0);
}

//复制文件
extern "C" __declspec(dllexport) void copyFile(char* src, char* dest);
void copyFile(char* src, char* dest) {
    if (CopyFile(_T(src), _T(dest), FALSE)) {
        cout << "copy,success" << endl;
    }
    else {
        cout << "copy,fail" << endl;
    }
}

//移动文件(剪切)
extern "C" __declspec(dllexport) void moveFile(char* src, char* dest);
void moveFile(char* src, char* dest) {
    if (MoveFileExA(src, dest,MOVEFILE_COPY_ALLOWED)) {
        cout << "move,success" << endl;
    }
    else {
        cout << "move,fail" << endl;
    }
}

//删除文件
extern "C" __declspec(dllexport) void deleteFile(char* src);
void deleteFile(char* src) {
    if (DeleteFileA(src)) {
        cout << "delete,success" << endl;
    }
    else {
        cout << "delete,fail" << endl;
    }
}

//创建文件
extern "C" __declspec(dllexport) bool createTxtFile(char* filePath);
bool createTxtFile(char* filePath)
{
	//判断文件是否存在
	if (PathFileExists(filePath))
	{
		cout << "the file is exist" << endl;
		return false;
	}

	HANDLE pFile = CreateFile(filePath, GENERIC_ALL, FILE_SHARE_READ, NULL, CREATE_NEW, FILE_ATTRIBUTE_NORMAL, NULL);
	if (pFile == NULL)
	{
		cout << "the file has been created fail" << endl;
		CloseHandle(pFile);
		return false;
	}
	else
	{
		cout << "the file has been created successfully" << endl;
		CloseHandle(pFile);
		return true;
	}
}

extern "C" __declspec(dllexport) void save();
void save()
{
	OPENFILENAME ofn = { 0 };
	TCHAR strFilename[MAX_PATH] = { 0 }; //用于接收文件名
	ofn.lStructSize = sizeof(OPENFILENAME);
	ofn.lpstrFile = strFilename;
	ofn.lpstrFilter = TEXT("文本(*.txt)\0*.txt*\0Cpp(*.cpp)\0*.cpp*\0图片(*.jpg)\0*.jpg*\0\0");
	ofn.nMaxFile = MAX_PATH;
	ofn.Flags = OFN_EXPLORER | OFN_HIDEREADONLY | OFN_NOCHANGEDIR | OFN_PATHMUSTEXIST | OFN_ALLOWMULTISELECT;
	ofn.lpstrTitle = TEXT("保存");
	//ofn.lpfnHook = (LPOFNHOOKPROC)SaveAsHookPrc;
	TCHAR* pszFileName;
	CString szDirectory;
	CString szFileName;
	//CStringArray szMultiFliePath; //如果允许多选，该array中记录着所有被选中的文件路径。
	if (GetOpenFileName(&ofn))
	{
		pszFileName = ofn.lpstrFile;
		szDirectory = pszFileName;
		//如果只允许单选， 缓冲区ofn.lpstrFile包含完整的文件路径（包括文件名以及扩展名）
		//如果允许多选，缓冲区ofn.lpstrFile指针包含内容分为两个部分，第一部分是选择文件所在的目录，第二部分是选择的所有文件名，每个文件名以“\0”作为分隔；
		//缓冲区中的最后一个字符串以两个空字符终止，因此可以通过移动指针来判断是否包含多个文件
		pszFileName = pszFileName + szDirectory.GetLength() + 1;
		while (*pszFileName)
		{
			szFileName = pszFileName;
			//szMultiFliePath.Add(szDirectory + pszFileName);
			pszFileName = pszFileName + szFileName.GetLength() + 1;
		}
	}
	//return true;
}
