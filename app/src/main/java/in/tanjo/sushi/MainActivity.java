package in.tanjo.sushi;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.tanjo.sushi.adapter.MainAdapter;
import in.tanjo.sushi.adapter.NavigationAdapter;
import in.tanjo.sushi.model.AbsNoteModel;
import in.tanjo.sushi.model.CountableSushiModel;
import in.tanjo.sushi.model.NoteManager;
import in.tanjo.sushi.model.NoteModel;
import in.tanjo.sushi.model.SushiModel;

public class MainActivity extends AppCompatActivity {

  private static final float THRESHOLD_HIDE_FLOATINGACTIONBUTTON = 25;

  @Bind(R.id.main_drawerlayout) DrawerLayout mDrawerLayout;
  @Bind(R.id.main_recycler_view) RecyclerView mMainRecyclerView;
  @Bind(R.id.main_toolbar) Toolbar mToolbar;
  @Bind(R.id.main_floating_action_button) FloatingActionButton mFloatingActionButton;
  @Bind(R.id.navigation_recycler_view) RecyclerView mNavigationRecyclerView;

  private int mScrollDist = 0;
  private NoteManager mNoteManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    init();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case EditNoteActivity.REQUESTCODE_NOTE_OBJECT: {
          receiveNote(data);
          break;
        }
        case AddSushiActivity.REQUESTCODE_SUSHI_OBJECT: {
          receiveSushi(data);
          break;
        }
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_create_new_note:
        if (!mNoteManager.contains(mNoteManager.getActiveNote())) {
          mNoteManager.add(mNoteManager.getActiveNote());
          mNoteManager.saveNoteList();
        }
        mNoteManager.setActiveNote(new NoteModel());
        mNoteManager.saveActiveNoteId();
        updateMainAdapter();
        mNavigationRecyclerView.getAdapter().notifyDataSetChanged();
        break;
      case R.id.action_settings:
        snackbar("設定は開発中です");
        // SettingsActivity.startActivity(this);
        break;
      case R.id.action_note_edit:
        EditNoteActivity.startActivityWithNoteObjectAndRequestCode(this, mNoteManager.getActiveNote());
        break;
      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @OnClick(R.id.main_floating_action_button)
  void add() {
    AddSushiActivity.startActivityWithSushiRequestCode(this);
  }

  /**
   * この Activity で SushiModel を受け取ったときに処理する.
   */
  private void receiveSushi(Intent data) {
    if (data == null) {
      return;
    }
    Bundle bundle = data.getExtras();
    if (bundle != null) {
      SushiModel sushiModel = (SushiModel) bundle.getSerializable(AddSushiActivity.BUNDLEKEY_SUSHI_MODEL);
      if (sushiModel != null) {
        addItem(new CountableSushiModel(sushiModel));
        snackbar(sushiModel.getName() + "を追加しました");
      }
    }
  }

  /**
   * この Activity で NoteModel を受け取ったときに処理する.
   */
  private void receiveNote(Intent data) {
    if (data == null) {
      return;
    }
    Bundle bundle = data.getExtras();
    if (bundle != null) {
      NoteModel noteModel = (NoteModel) bundle.getSerializable(EditNoteActivity.BUNDLEKEY_NOTE_OBJECT);
      if (noteModel != null) {
        mNoteManager.setActiveNote(noteModel);
        updateMainAdapter();
        snackbar("メモを更新しました");
        mNoteManager.saveActiveNoteId();
        mNoteManager.replace(mNoteManager.getActiveNote());
        mNoteManager.saveNoteList();
        mNavigationRecyclerView.getAdapter().notifyDataSetChanged();
      }
    }
  }

  /**
   * Snackbar を表示させる.
   */
  private void snackbar(String text) {
    final Snackbar snackbar = Snackbar.make(mDrawerLayout, text, Snackbar.LENGTH_LONG);
    snackbar.setAction("OK", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        snackbar.dismiss();
      }
    });
    snackbar.show();
  }

  /**
   * イニシャライザー
   */
  private void init() {
    initVariables();
    initToolbar();
    initMainRecyclerView();
    initNavigaitonRecyclerView();
  }

  /**
   * 変数の初期化
   */
  private void initVariables() {
    mNoteManager = new NoteManager(this);
  }

  /**
   * ツールバーの初期化
   */
  private void initToolbar() {
    setSupportActionBar(mToolbar);
    mToolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDrawerLayout.openDrawer(mNavigationRecyclerView);
      }
    });
  }

  /**
   * メインのリサイクルViewを初期化
   */
  private void initMainRecyclerView() {
    mMainRecyclerView.setHasFixedSize(true);
    mMainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mMainRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (mFloatingActionButton.isShown() && mScrollDist > THRESHOLD_HIDE_FLOATINGACTIONBUTTON) {
          mFloatingActionButton.hide();
          mScrollDist = 0;
        } else if (!mFloatingActionButton.isShown() && mScrollDist < -THRESHOLD_HIDE_FLOATINGACTIONBUTTON) {
          mFloatingActionButton.show();
        }

        if ((mFloatingActionButton.isShown() && dy > 0) || (!mFloatingActionButton.isShown() && dy < 0)) {
          mScrollDist += dy;
        }
      }
    });

    updateMainAdapter();
  }

  private void updateMainAdapter() {
    MainAdapter mainAdapter = new MainAdapter(mNoteManager.getActiveNote().getSushiModelList());
    mainAdapter.setOnMainAdapterItemClickListener(new MainAdapter.OnMainAdapterItemClickListener() {
      @Override
      public void onItemClick(View v, MainAdapter adapter, int position, final CountableSushiModel countableSushiModel) {
        countableSushiModel.setCount(countableSushiModel.getCount() + 1);
        changeItem(position, countableSushiModel);
      }

      @Override
      public void onItemLongClick(View v, MainAdapter adapter, final int position, final CountableSushiModel countableSushiModel) {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
        popupMenu.getMenuInflater().inflate(R.menu.popup_countable_sushi_model_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
              case R.id.popup_menu_sushi_delete:
                removeItem(position, countableSushiModel);
                snackbar(countableSushiModel.getName() + "を削除しました");
                break;
              case R.id.popup_menu_sushi_plus1:
                countableSushiModel.setCount(countableSushiModel.getCount() + 1);
                changeItem(position, countableSushiModel);
                break;
              case R.id.popup_menu_sushi_minus1:
                if (countableSushiModel.getCount() - 1 >= 0) {
                  countableSushiModel.setCount(countableSushiModel.getCount() - 1);
                }
                changeItem(position, countableSushiModel);
                break;
            }
            return true;
          }
        });
        popupMenu.show();
      }
    });
    mMainRecyclerView.setAdapter(mainAdapter);
  }

  private void addItem(CountableSushiModel sushiModel) {
    mNoteManager.getActiveNote().getSushiModelList().add(sushiModel);
    mMainRecyclerView.getAdapter().notifyItemInserted(mNoteManager.getActiveNote().getSushiModelList().size());
    mNoteManager.saveActiveNoteId();
  }

  private void removeItem(int position, CountableSushiModel sushiModel) {
    mNoteManager.getActiveNote().getSushiModelList().remove(sushiModel);
    mMainRecyclerView.getAdapter().notifyItemRemoved(position);
    mNoteManager.saveActiveNoteId();
    // スクロールができなくなるとなにもできなくなるので強制的に表示してあげる.
    mFloatingActionButton.show();
  }

  private void changeItem(int position, CountableSushiModel sushiModel) {
    mNoteManager.getActiveNote().getSushiModelList().set(position, sushiModel);
    mMainRecyclerView.getAdapter().notifyItemChanged(position);
    mNoteManager.saveActiveNoteId();
  }

  /**
   * ナビゲーションのリサイクルViewを初期化
   */
  private void initNavigaitonRecyclerView() {
    mNavigationRecyclerView.setHasFixedSize(true);
    mNavigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    updateNavigationAdapter();
  }

  private void updateNavigationAdapter() {
    NavigationAdapter navigationAdapter = new NavigationAdapter(mNoteManager.getNotesModel().getNotes());
    navigationAdapter.setOnNavigationAdapterItemClickListener(new NavigationAdapter.OnNavigationAdapterItemClickListener() {
      @Override
      public void onItemClick(View v, NavigationAdapter adapter, int position, AbsNoteModel noteModel) {
        if (!mNoteManager.contains(mNoteManager.getActiveNote())) {
          mNoteManager.add(mNoteManager.getActiveNote());
          mNoteManager.saveNoteList();
        }
        mNoteManager.setActiveNote(mNoteManager.getNote(noteModel.getId()));
        mNoteManager.saveActiveNoteId();
        updateMainAdapter();
        mNavigationRecyclerView.getAdapter().notifyDataSetChanged();
        mDrawerLayout.closeDrawers();
      }

      @Override
      public void onItemLongClick(View v, NavigationAdapter adapter, int position, AbsNoteModel noteModel) {
        // TODO: 削除

      }
    });
    mNavigationRecyclerView.setAdapter(navigationAdapter);
  }
}
