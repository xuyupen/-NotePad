package com.example.android.notepad;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteSearch extends ListActivity {
    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,//在这里加入了修改时间的显示
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search);


        Intent intent = getIntent();// Gets the intent that started this Activity.
// If there is no data associated with the Intent, sets the data to the default URI, which
// accesses a list of notes.
        if (intent.getData()==null){
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        getListView().setOnCreateContextMenuListener(this);

        SearchView mSearchView = (SearchView) findViewById(R.id.search_view);//注册监听器
        mSearchView.setIconifiedByDefault(false); //显示搜索的天幕，默认只有一个放大镜图标
        mSearchView.setSubmitButtonEnabled(true); //显示搜索按钮
        mSearchView.setBackgroundColor(getResources().getColor(R.color.blue_green)); //设置背景颜色
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
            SimpleCursorAdapter.ViewBinder viewBinder=new SimpleCursorAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int i)
                    {
                        if(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE)==i){

                            TextView textView1=(TextView)view;
                            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.CHINA);
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
                // Sets the ListView's adapter to be the cursor adapter that was just created.
                setListAdapter(adapter);
                return true;
            }
        });
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // Constructs a new URI from the incoming URI and the row ID
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        // Gets the action from the incoming Intent
        String action = getIntent().getAction();

        // Handles requests for note data
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

            // Sets the result to return to the component that called this Activity. The
            // result contains the new URI
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {

            // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
            // Intent's data is the note ID URI. The effect is to call NoteEdit.
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}
