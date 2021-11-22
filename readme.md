# 期中实验NotePad

## 1.添加时间戳

 

1. 添加一个TextView用于显示时间戳

2. 在PROJECTION（Activity用到的数据）中添加NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE。

   将其加入dataColumns中，将其和viewIDs中我们新加的TextView的id绑定起来。

3. 此时在NoteEditor.java的updateNote将可将修改时间存入但格式未修改

4. 格式的修改有多种方法，可以在NotePadProvider中修改，也可以在NoteList里修改。我选择在NoteList中修改。具体方法其实

   也很简单，使用SimpleCursorAdapt.ViewBinder，重写它的setviewValue方法。

​       关键代码：

​        

```java
 SimpleCursorAdapter.ViewBinder viewBinder=new SimpleCursorAdapter.ViewBinder() {
    @Override
  public boolean setViewValue(View view, Cursor cursor, int i) 
  {
         if(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE)==i){

             TextView textView1=(TextView)view;
             SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss",Locale.CHINA);
             Date date=new Date(cursor.getLong(i));
             String time=format.format(date);
             Log.d("TIME", "onCreate1:"+time);
             textView1.setText(time);
             return true;
           }
           return false;
       }
};
adapter.setViewBinder(viewBinder);

```

实现截图：

![](https://github.com/xuyupen/NotePad/blob/ada1d593a2621b330bc7ddad92f0e2a027ac95eb/%E6%97%B6%E9%97%B4%E6%88%B3.png)

## 2.笔记搜索（按标题）

1.在list_options_menu.xml的菜单中添加一个搜索的item

2.在NoteList的onOptionItemSelected方法的switch中添加case，当case匹配时（点击搜索图标）跳转到搜索界面

关键代码：

```java
case R.id.menu_search:
          startActivity(new Intent(Intent.ACTION_SEARCH,getIntent().getData()));
          return true;
```

3.在AndroidManifest.xml清单文件中加入声明

关键代码：

```java
<activity
    android:name=".NoteSearch"
    android:label="NoteSearch"
    >

    <intent-filter>
        <action android:name="android.intent.action.NoteSearch" />
        <action android:name="android.intent.action.SEARCH" />
        <action android:name="android.intent.action.SEARCH_LONG_PRESS" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="vnd.android.cursor.dir/vnd.google.note" />
        <!--1.vnd.android.cursor.dir代表返回结果为多列数据-->
        <!--2.vnd.android.cursor.item 代表返回结果为单列数据-->
    </intent-filter>
</activity>
```

4.创建搜索界面的布局note_search.xml

5.搜索用SearchView实现，通过SearchView.OnQueryTextListener接口来完成查询，NoteSearch的实现其实和NoteList类似，反而功能更简单。

关键代码：

```java
mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) { //Test改变的时候执行的内容
                                                   //Text发生改变时执行的内容
            String selection = NotePad.Notes.COLUMN_NAME_TITLE + " Like ? ";//查询条件
            String[] selectionArgs = { "%"+s+"%" };//查询条件参数，配合selection参数使用,%通配多个字符

            //查询数据库中的内容,当我们使用 SQLiteDatabase.query()方法时，就会得到Cursor对象， Cursor所指向的就是每一条数据。
            //managedQuery(Uri, String[], String, String[], String)等同于Context.getContentResolver().query()
            Cursor cursor = managedQuery(
                                         getIntent().getData(),            // Use the default content URI for the provider.用于ContentProvider查询的URI，从这个URI获取数据
                                         PROJECTION,                       // Return the note ID and title for each note. and modifcation date.用于标识uri中有哪些columns需要包含在返回的Cursor对象中
                                         selection,                        // 作为查询的过滤参数，也就是过滤出符合selection的数据，类似于SQL的Where语句之后的条件选择
                                         selectionArgs,                    // 查询条件参数，配合selection参数使用
                                         NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.查询结果的排序方式，按照某个columns来排序，例：String sortOrder = NotePad.Notes.COLUMN_NAME_TITLE
                                         );

                                         //一个简单的适配器，将游标中的数据映射到布局文件中的TextView控件或者ImageView控件中
            String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE ,  NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE };
            int[] viewIDs = { android.R.id.text1 , android.R.id.text2 };
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                                                           NoteSearch.this,                   //context:上下文
                                                           R.layout.noteslist_item,         //layout:布局文件，至少有int[]的所有视图
                                                           cursor,                          //cursor：游标
                                                           dataColumns,                     //from：绑定到视图的数据
                                                           viewIDs                          //to:用来展示from数组中数据的视图
                                                           //flags：用来确定适配器行为的标志，Android3.0之后淘汰
                                                   );
```

实现截图：

![](https://github.com/xuyupen/NotePad/blob/86a5085e77a90630df26b096d166852d6189d551/%E6%90%9C%E7%B4%A2.png)

## 3.设置编辑笔记时的背景颜色

1.在NotePad契约类中添加COLUMN_NAME_BACK_COLOR，设置5种预定的值，根据不同的int值显示对应的颜色

关键代码：

```java
public static final String COLUMN_NAME_BACK_COLOR = "color";
        public static final int DEFAULT_COLOR = 0;
        public static final int YELLOW_COLOR = 1;
        public static final int BLUE_COLOR = 2;
        public static final int GREEN_COLOR = 3;
        public static final int RED_COLOR = 4;
```

2.在数据库中添加颜色字段，并在NotePadProvider的静态构造块中加入相应的键值对。

关键代码：

```java
sNotesProjectionMap.put(
        NotePad.Notes.COLUMN_NAME_BACK_COLOR,
        NotePad.Notes.COLUMN_NAME_BACK_COLOR);
```

3.如果此前已经生产数据库，此时需要将数据库删除在生成，否则会出现问题。到此添加背景颜色的准备工作已经做完。

4.在NoteEditor的PROJECTION中COLUMN_NAME_BACK_COLOR，

修改onResume()方法（从数据库读取颜色并设置编辑界面背景色操作放入其中）

关键代码：

```java
//读取颜色数据
int x = mCursor.getInt(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR));
    /**
    * 白 255 255 255
    * 黄 247 216 133
    * 蓝 165 202 237
    * 绿 161 214 174
    * 红 244 149 133
    */
    switch (x){
        case NotePad.Notes.DEFAULT_COLOR:
            mText.setBackgroundColor(Color.rgb(255, 255, 255));
            break;
        case NotePad.Notes.YELLOW_COLOR:
            mText.setBackgroundColor(Color.rgb(247, 216, 133));
            break;
        case NotePad.Notes.BLUE_COLOR:
            mText.setBackgroundColor(Color.rgb(165, 202, 237));
            break;
        case NotePad.Notes.GREEN_COLOR:
            mText.setBackgroundColor(Color.rgb(161, 214, 174));
            break;
        case NotePad.Notes.RED_COLOR:
            mText.setBackgroundColor(Color.rgb(244, 149, 133));
            break;
        default:
            mText.setBackgroundColor(Color.rgb(255, 255, 255));
            break;
    }
```

5.在editor_options_menu.xml添加修改背景颜色的item，在NoteEditor的onOptionsItemSelected()方法，在菜单的switch中添加点击响应

关键代码：

```java
case R.id.menu_color://跳转改变颜色的activity
    Intent intent = new Intent(null,mUri);
    intent.setClass(NoteEditor.this,NoteColor.class);
    NoteEditor.this.startActivity(intent);
    break;
```

6.新建布局note_color.xml（NoteColor的contentView）

7.创建NoteColor的Acitvity

关键代码：

```java
package com.example.android.notepad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class NoteColor extends Activity {
    private Cursor mCursor;
    private Uri mUri;
    private int color;
    private static final int COLUMN_INDEX_TITLE = 1;
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_BACK_COLOR,
    };
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_color);
        mUri = getIntent().getData();
        mCursor = managedQuery(
                mUri,
                PROJECTION,
                null,
                null,
                null
        );
    }
    @SuppressLint("Range")
    @Override
    protected void onResume(){
        if(mCursor.moveToFirst()){
            color = mCursor.getInt(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR));
            Log.i("NoteColor", "before"+color);
        }
        super.onResume();
    }
    @Override
    protected void onPause() {
        //将修改的颜色存入数据库
        super.onPause();
        ContentValues values = new ContentValues();
        Log.i("NoteColor", "cun"+color);
        values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, color);
        getContentResolver().update(mUri, values, null, null);
        int x = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR);
        int y = mCursor.getInt(x);
        Log.i("NoteColor", "du"+y);
    }
    public void white(View view){
        color = NotePad.Notes.DEFAULT_COLOR;
        finish();
    }
    public void yellow(View view){
        color = NotePad.Notes.YELLOW_COLOR;
        finish();
    }
    public void blue(View view){
        color = NotePad.Notes.BLUE_COLOR;
        finish();
    }
    public void green(View view){
        color = NotePad.Notes.GREEN_COLOR;
        finish();
    }
    public void red(View view){
        color = NotePad.Notes.RED_COLOR;
        finish();
    }
}


```

8.在清单文件中加入相关声明，不在赘述.

9.（拓展）将Notelist的背景和编辑时的背景串联在一起。有实现此处不做说明。

实现截图：

![](https://github.com/xuyupen/NotePad/blob/86a5085e77a90630df26b096d166852d6189d551/%E8%83%8C%E6%99%AF%E9%80%89%E6%8B%A9%E5%AF%B9%E8%AF%9D%E6%A1%86.png)

![](https://github.com/xuyupen/NotePad/blob/86a5085e77a90630df26b096d166852d6189d551/%E8%83%8C%E6%99%AF%E4%BF%AE%E6%94%B9%E5%AE%9E%E4%BE%8B.png)

## 4.笔记排序

1.在list_options_menu.xml中添加排序的item，里面嵌套一个menu（菜单）包含按颜色，修改时间，创建时间三个item。

2.在NoteList的onOptionsItemSelected方法switch里添加新的3个case，分别对应点击事件按颜色排序，按创建时间排序，按修改时间排序。

关键代码：

```java
    //创建时间排序
        case R.id.menu_sort1:
                cursor = managedQuery(
                        getIntent().getData(),
                        PROJECTION,
                        null,
                        null,
                        NotePad.Notes._ID
                );
                adapter = new SimpleCursorAdapter(
                        this,
                        R.layout.noteslist_item,
                        cursor,
                        dataColumns,
                        viewIDs
                );
                adapter.setViewBinder(viewBinder);
                setListAdapter(adapter);
                return true;
            //修改时间排序
        case R.id.menu_sort2:
                cursor = managedQuery(
                        getIntent().getData(),
                        PROJECTION,
                        null,
                        null,
                        NotePad.Notes.DEFAULT_SORT_ORDER
                );
                adapter = new SimpleCursorAdapter(
                        this,
                        R.layout.noteslist_item,
                        cursor,
                        dataColumns,
                        viewIDs
                );
                adapter.setViewBinder(viewBinder);
                setListAdapter(adapter);
                return true;
            //颜色排序
        case R.id.menu_sort3:
                cursor = managedQuery(
                        getIntent().getData(),
                        PROJECTION,
                        null,
                        null,
                        NotePad.Notes.COLUMN_NAME_BACK_COLOR
                );
                adapter = new SimpleCursorAdapter(
                        this,
                        R.layout.noteslist_item,
                        cursor,
                        dataColumns,
                        viewIDs
                );
                adapter.setViewBinder(viewBinder);
                setListAdapter(adapter);
                return true;
```

3.排序多次使用到cursor，adapter，所以将adapter,cursor,dataColumns,viewIDs，SimpleCursorAdapter.ViewBinder定义在函数外类内。

实现截图：

![](https://github.com/xuyupen/NotePad/blob/86a5085e77a90630df26b096d166852d6189d551/%E6%8E%92%E5%BA%8F%E8%8F%9C%E5%8D%95.png)

按创建时间排序：

![](https://github.com/xuyupen/NotePad/blob/86a5085e77a90630df26b096d166852d6189d551/%E5%88%9B%E5%BB%BA%E6%97%B6%E9%97%B4sort.png)

按修改时间排序：

![](https://github.com/xuyupen/NotePad/blob/86a5085e77a90630df26b096d166852d6189d551/%E4%BF%AE%E6%94%B9%E6%97%B6%E9%97%B4sort.png)

按颜色排序：

![](https://github.com/xuyupen/NotePad/blob/86a5085e77a90630df26b096d166852d6189d551/%E9%A2%9C%E8%89%B2%E6%8E%92%E5%BA%8Fsort.png)
