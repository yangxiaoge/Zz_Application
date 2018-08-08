package com.yjn.common.baserx;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by yang.jinan on 2017/8/8.
 */

public class RxManager {
    /*管理Observables 和 Subscribers订阅*/
    private CompositeDisposable mCompositeSubscription = new CompositeDisposable();

    /**
     * 单纯的Observables 和 Subscribers管理
     */
    public void add(Disposable disposable) {
        /*订阅管理*/
        mCompositeSubscription.add(disposable);
    }

    /**
     * 单个presenter生命周期结束，取消订阅和所有rxbus观察
     */
    public void clear() {
        mCompositeSubscription.clear();// 取消所有订阅
    }
}