package com.raizlabs.android.dbflow.structure;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.BaseAsyncObject;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.lang.ref.WeakReference;

/**
 * Description: Called from a {@link BaseModel}, this places the current {@link Model} interaction on the background.
 */
public class AsyncModel<TModel> extends BaseAsyncObject<AsyncModel<TModel>> implements Model {

    /**
     * Listens for when this {@link Model} modification completes.
     */
    public interface OnModelChangedListener<T> {

        /**
         * Called when the change finishes on the {@link DefaultTransactionQueue}. This method is called on the UI thread.
         */
        void onModelChanged(T model);
    }

    private final TModel model;
    private transient WeakReference<OnModelChangedListener<TModel>> onModelChangedListener;

    private ModelAdapter<TModel> modelAdapter;

    public AsyncModel(@NonNull TModel referenceModel) {
        super(referenceModel.getClass());
        model = referenceModel;
    }

    /**
     * Call before {@link #save()}, {@link #delete()}, {@link #update()}, or {@link #insert()}.
     *
     * @param onModelChangedListener The listener to use for a corresponding call to a method.
     */
    public AsyncModel<TModel> withListener(OnModelChangedListener<TModel> onModelChangedListener) {
        this.onModelChangedListener = new WeakReference<>(onModelChangedListener);
        return this;
    }

    private ModelAdapter<TModel> getModelAdapter() {
        if (modelAdapter == null) {
            //noinspection unchecked
            modelAdapter = (ModelAdapter<TModel>) FlowManager.getModelAdapter(model.getClass());
        }
        return modelAdapter;
    }

    @Override
    public boolean save() {
        executeTransaction(new ProcessModelTransaction.Builder<>(
                new ProcessModelTransaction.ProcessModel<TModel>() {
                    @Override
                    public void processModel(TModel model, DatabaseWrapper wrapper) {
                        getModelAdapter().save(model, wrapper);
                    }
                }).add(model).build());
        return false;
    }

    @Override
    public boolean delete() {
        executeTransaction(new ProcessModelTransaction.Builder<>(
                new ProcessModelTransaction.ProcessModel<TModel>() {
                    @Override
                    public void processModel(TModel model, DatabaseWrapper wrapper) {
                        getModelAdapter().delete(model, wrapper);
                    }
                }).add(model).build());
        return false;
    }

    @Override
    public boolean update() {
        executeTransaction(new ProcessModelTransaction.Builder<>(
                new ProcessModelTransaction.ProcessModel<TModel>() {
                    @Override
                    public void processModel(TModel model, DatabaseWrapper wrapper) {
                        getModelAdapter().update(model, wrapper);
                    }
                }).add(model).build());
        return false;
    }

    @Override
    public long insert() {
        executeTransaction(new ProcessModelTransaction.Builder<>(
                new ProcessModelTransaction.ProcessModel<TModel>() {
                    @Override
                    public void processModel(TModel model, DatabaseWrapper wrapper) {
                        getModelAdapter().insert(model, wrapper);
                    }
                }).add(model).build());
        return INVALID_ROW_ID;
    }

    @Override
    public void load() {
        executeTransaction(new ProcessModelTransaction.Builder<>(
                new ProcessModelTransaction.ProcessModel<TModel>() {
                    @Override
                    public void processModel(TModel model, DatabaseWrapper wrapper) {
                        getModelAdapter().load(model, wrapper);
                    }
                }).add(model).build());
    }

    @Override
    public boolean exists() {
        return getModelAdapter().exists(model);
    }

    @Override
    protected void onSuccess(Transaction transaction) {
        if (onModelChangedListener != null && onModelChangedListener.get() != null) {
            onModelChangedListener.get().onModelChanged(model);
        }
    }
}
