package com.yjn.common.baserx;

import android.content.Context;

import com.yjn.common.R;
import com.yjn.common.util.NetWorkUtils;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by yang.jinan on 2017/8/16.
 */

public abstract class RxDisposableObserver<T> extends DisposableObserver<T> {

    private Context mContext;

    public RxDisposableObserver(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onNext(@NonNull T t) {
        _onNext(t);
    }

    @Override
    public void onError(@NonNull Throwable e) {
        e.printStackTrace();
        //网络
        if (!NetWorkUtils.isNetConnected(mContext)) {
            _onError(mContext.getString(R.string.no_net));
        }
        //其它
        else {
            _onError(mContext.getString(R.string.net_error));
        }
    }

    @Override
    public void onComplete() {

    }


    protected abstract void _onNext(T t);

    protected abstract void _onError(String message);
}
