package com.yzx.chat.mvp.view.fragment;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.mvp.contract.ContactOperationContract;
import com.yzx.chat.mvp.presenter.ContactOperationPresenter;
import com.yzx.chat.mvp.view.activity.StrangerProfileActivity;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.adapter.ContactOperationAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;
import com.yzx.chat.widget.view.OverflowMenuShowHelper;
import com.yzx.chat.widget.view.OverflowPopupMenu;
import com.yzx.chat.widget.view.ProgressDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class ContactOperationFragment extends BaseFragment<ContactOperationContract.Presenter> implements ContactOperationContract.View {


    private RecyclerView mRecyclerView;
    private View mFooterView;
    private TextView mTvLoadMoreHint;
    private OverflowPopupMenu mContactOperationMenu;
    private ProgressDialog mProgressDialog;
    private ContactOperationAdapter mAdapter;
    private List<ContactOperationBean> mContactOperationList;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_operation;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.ContactOperationFragment_mRecyclerView);
        mFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) parentView, false);
        mTvLoadMoreHint = mFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mProgressDialog = new ProgressDialog(mContext, getString(R.string.ProgressHint_Add));
        mContactOperationMenu = new OverflowPopupMenu(mContext);
        mContactOperationList = new ArrayList<>(32);
        mAdapter = new ContactOperationAdapter(mContactOperationList);
    }

    @Override
    protected void setup() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(mContext, R.color.dividerColorBlack), DividerItemDecoration.ORIENTATION_HORIZONTAL));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(mOnRecyclerViewItemClickListener);
        ((DefaultItemAnimator) (mRecyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        mAdapter.setOnAcceptContactRequestListener(mOnAcceptContactRequestListener);

        mTvLoadMoreHint.setText(R.string.LoadMoreHint_Default);

        setOverflowMenu();

        setData();
    }


    private void setOverflowMenu() {
        mContactOperationMenu.setWidth((int) AndroidUtil.dip2px(128));
        mContactOperationMenu.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.backgroundColorWhite)));
        mContactOperationMenu.setElevation(AndroidUtil.dip2px(2));
        mContactOperationMenu.inflate(R.menu.menu_contact_message_overflow);
        mContactOperationMenu.setOnMenuItemClickListener(new OverflowPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, int menuID) {
                int index = (int) mRecyclerView.getTag();
                if (menuID == R.id.ContactOperationMenu_Delete) {
                    mPresenter.removeContactOperation(mContactOperationList.get(index));
                }
            }
        });
    }

    private void setData() {
        mPresenter.init();
    }

    public void enableLoadMoreHint(boolean isEnable) {
        if (isEnable) {
            mAdapter.setFooterView(mFooterView);
        } else {
            mAdapter.setFooterView(null);
        }
    }


    private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {

        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            mRecyclerView.setTag(position);
            OverflowMenuShowHelper.show(viewHolder.itemView, mContactOperationMenu, mRecyclerView.getHeight(), (int) touchX, (int) touchY);
        }
    };

    private final ContactOperationAdapter.OnAcceptContactRequestListener mOnAcceptContactRequestListener = new ContactOperationAdapter.OnAcceptContactRequestListener() {
        @Override
        public void onAcceptContactRequest(int position) {
            mPresenter.acceptContactRequest(mContactOperationList.get(position));
        }

        @Override
        public void enterDetails(int position) {
            Intent intent = new Intent(mContext, StrangerProfileActivity.class);
            intent.putExtra(StrangerProfileActivity.INTENT_EXTRA_CONTENT_OPERATION, mContactOperationList.get(position));
            startActivity(intent);
        }
    };

    @Override
    public ContactOperationContract.Presenter getPresenter() {
        return new ContactOperationPresenter();
    }

    @Override
    public void addContactOperationToList(ContactOperationBean ContactOperation) {
        mAdapter.notifyItemInsertedEx(0);
        mContactOperationList.add(0, ContactOperation);
        enableLoadMoreHint(mContactOperationList.size() > 12);
    }

    @Override
    public void removeContactOperationFromList(ContactOperationBean ContactOperation) {
        int removePosition = mContactOperationList.indexOf(ContactOperation);
        if (removePosition >= 0) {
            mAdapter.notifyItemRemovedEx(removePosition);
            mContactOperationList.remove(removePosition);
            enableLoadMoreHint(mContactOperationList.size() > 12);
        } else {
            LogUtil.e("remove ContactOperationItem fail in ui");
        }
    }

    @Override
    public void updateContactOperationFromList(ContactOperationBean ContactOperation) {
        int index = mContactOperationList.indexOf(ContactOperation);
        if (index < 0) {
            LogUtil.e("update fail from ContactOperationList");
        } else {
            mAdapter.notifyItemChangedEx(index);
            mContactOperationList.set(index, ContactOperation);
        }
    }

    @Override
    public void updateAllContactOperationList(DiffUtil.DiffResult diffResult, List<ContactOperationBean> newDataList) {
        diffResult.dispatchUpdatesTo(new BaseRecyclerViewAdapter.ListUpdateCallback(mAdapter));
        mContactOperationList.clear();
        mContactOperationList.addAll(newDataList);
        enableLoadMoreHint(mContactOperationList.size() > 12);
    }

    @Override
    public void setEnableProgressDialog(boolean isEnable) {
        if (isEnable) {
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void showError(String error) {
        showToast(error);
    }
}
