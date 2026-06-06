package com.example.e_tradeandroid.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.model.TradeStateMachine;
import com.example.e_tradeandroid.model.User;
import com.example.e_tradeandroid.repository.TradeRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易 ViewModel - 管理交易页面的所有数据和状态
 */
public class TradeViewModel extends AndroidViewModel {
    private static final String TAG = "TradeViewModel";
    
    private final TradeRepository tradeRepository;
    
    // 交易数据
    private final MutableLiveData<TradeInfo> tradeInfo = new MutableLiveData<>();
    private final MutableLiveData<Product> product = new MutableLiveData<>();
    private final MutableLiveData<User> buyer = new MutableLiveData<>();
    private final MutableLiveData<User> seller = new MutableLiveData<>();
    
    // 状态机
    private TradeStateMachine stateMachine;
    
    // UI 状态
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSubmitEnabled = new MutableLiveData<>(false);
    
    // 表单数据
    private final MutableLiveData<String> buyerPhone = new MutableLiveData<>("");
    private final MutableLiveData<String> sellerPhone = new MutableLiveData<>("");
    private final MutableLiveData<String> meetingLocation = new MutableLiveData<>("");
    private final MutableLiveData<String> meetingTime = new MutableLiveData<>("");
    
    // 初始化状态
    private final MutableLiveData<Boolean> productLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> tradeLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> userLoaded = new MutableLiveData<>(false);
    
    // 版本号（乐观锁）
    private int version = 0;
    
    public TradeViewModel(@NonNull Application application) {
        super(application);
        tradeRepository = new TradeRepository();
    }
    
    // ==================== Getter Methods ====================
    
    public LiveData<TradeInfo> getTradeInfo() { return tradeInfo; }
    public LiveData<Product> getProduct() { return product; }
    public LiveData<User> getBuyer() { return buyer; }
    public LiveData<User> getSeller() { return seller; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsSubmitEnabled() { return isSubmitEnabled; }
    public LiveData<String> getBuyerPhone() { return buyerPhone; }
    public LiveData<String> getSellerPhone() { return sellerPhone; }
    public LiveData<String> getMeetingLocation() { return meetingLocation; }
    public LiveData<String> getMeetingTime() { return meetingTime; }
    
    public TradeStateMachine getStateMachine() { return stateMachine; }
    
    // ==================== Data Loading ====================
    
    /**
     * 加载商品信息
     */
    public void loadProduct(long productId) {
        if (productId <= 0) {
            errorMessage.postValue("商品ID无效");
            return;
        }
        
        isLoading.postValue(true);
        // 实际项目中应调用商品仓库
        productLoaded.postValue(true);
        checkDataReady();
        isLoading.postValue(false);
    }
    
    /**
     * 加载交易详情
     */
    public void loadTrade(long tradeId) {
        if (tradeId <= 0) {
            errorMessage.postValue("交易ID无效");
            return;
        }
        
        isLoading.postValue(true);
        tradeRepository.getTradeDetail(tradeId, new TradeRepository.TradeCallback<TradeInfo>() {
            @Override
            public void onSuccess(TradeInfo data) {
                tradeInfo.postValue(data);
                stateMachine = new TradeStateMachine(data.getTradeStatus());
                version++;
                
                // 填充表单数据
                if (data.getMeetingLocation() != null) {
                    meetingLocation.postValue(data.getMeetingLocation());
                }
                if (data.getMeetingTime() != null) {
                    meetingTime.postValue(data.getMeetingTime());
                }
                if (data.getBuyerPhone() != null) {
                    buyerPhone.postValue(data.getBuyerPhone());
                }
                if (data.getSellerPhone() != null) {
                    sellerPhone.postValue(data.getSellerPhone());
                }
                
                tradeLoaded.postValue(true);
                checkDataReady();
                isLoading.postValue(false);
            }
            
            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 加载用户信息
     */
    public void loadUsers(long buyerId, long sellerId) {
        userLoaded.postValue(true);
        checkDataReady();
    }
    
    /**
     * 检查所有必要数据是否就绪
     */
    private void checkDataReady() {
        boolean ready = productLoaded.getValue() != null && productLoaded.getValue() &&
                       tradeLoaded.getValue() != null && tradeLoaded.getValue() &&
                       userLoaded.getValue() != null && userLoaded.getValue();
        isSubmitEnabled.postValue(ready);
        Log.d(TAG, "数据就绪检查: " + ready);
    }
    
    // ==================== Trade Operations ====================
    
    /**
     * 创建交易
     */
    public void createTrade(long productId, String meetingTime, String meetingLocation,
                           String buyerPhone, TradeRepository.TradeCallback<TradeInfo> callback) {
        if (!isDataReady()) {
            callback.onFailure("数据未就绪");
            return;
        }
        
        isLoading.postValue(true);
        tradeRepository.createTrade(productId, meetingTime, meetingLocation, new TradeRepository.TradeCallback<TradeInfo>() {
            @Override
            public void onSuccess(TradeInfo data) {
                tradeInfo.postValue(data);
                stateMachine = new TradeStateMachine(data.getTradeStatus());
                version++;
                isLoading.postValue(false);
                callback.onSuccess(data);
            }
            
            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
                callback.onFailure(error);
            }
        });
    }
    
    /**
     * 卖家确认交易
     */
    public void confirmTrade(long tradeId, String sellerPhone, 
                           TradeRepository.TradeCallback<Void> callback) {
        if (!canConfirm()) {
            callback.onFailure("当前状态不允许确认");
            return;
        }
        
        isLoading.postValue(true);
        tradeRepository.confirmTrade(tradeId, sellerPhone, new TradeRepository.TradeCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (stateMachine != null) {
                    stateMachine.transitionTo(com.example.e_tradeandroid.model.TradeStatus.CONFIRMED, "SELLER");
                }
                version++;
                isLoading.postValue(false);
                callback.onSuccess(null);
            }
            
            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
                callback.onFailure(error);
            }
        });
    }
    
    /**
     * 完成交易（卖家发货）
     */
    public void completeTrade(long tradeId, TradeRepository.TradeCallback<Void> callback) {
        if (!canComplete()) {
            callback.onFailure("当前状态不允许完成");
            return;
        }
        
        isLoading.postValue(true);
        tradeRepository.completeTrade(tradeId, new TradeRepository.TradeCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (stateMachine != null) {
                    stateMachine.transitionTo(com.example.e_tradeandroid.model.TradeStatus.SELLER_COMPLETED, "SELLER");
                }
                version++;
                isLoading.postValue(false);
                callback.onSuccess(null);
            }
            
            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
                callback.onFailure(error);
            }
        });
    }
    
    /**
     * 确认收货（买家）
     */
    public void confirmReceive(long tradeId, TradeRepository.TradeCallback<Void> callback) {
        if (!canConfirmReceive()) {
            callback.onFailure("当前状态不允许确认收货");
            return;
        }
        
        isLoading.postValue(true);
        tradeRepository.completeTrade(tradeId, new TradeRepository.TradeCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (stateMachine != null) {
                    stateMachine.transitionTo(com.example.e_tradeandroid.model.TradeStatus.COMPLETED, "BUYER");
                }
                version++;
                isLoading.postValue(false);
                callback.onSuccess(null);
            }
            
            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
                callback.onFailure(error);
            }
        });
    }
    
    /**
     * 取消交易
     */
    public void cancelTrade(long tradeId, TradeRepository.TradeCallback<Void> callback) {
        if (!canCancel()) {
            callback.onFailure("当前状态不允许取消");
            return;
        }
        
        isLoading.postValue(true);
        tradeRepository.cancelTrade(tradeId, new TradeRepository.TradeCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (stateMachine != null) {
                    stateMachine.transitionTo(com.example.e_tradeandroid.model.TradeStatus.CANCELLED, "USER");
                }
                version++;
                isLoading.postValue(false);
                callback.onSuccess(null);
            }
            
            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
                callback.onFailure(error);
            }
        });
    }
    
    /**
     * 更新交易信息（修改）
     */
    public void updateTrade(long tradeId, String meetingTime, String meetingLocation,
                           TradeRepository.TradeCallback<Void> callback) {
        if (!canUpdate()) {
            callback.onFailure("当前状态不允许修改");
            return;
        }
        
        isLoading.postValue(true);
        tradeRepository.updateTrade(tradeId, meetingTime, meetingLocation, new TradeRepository.TradeCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // 进入待确认修改状态
                if (stateMachine != null) {
                    stateMachine.transitionTo(com.example.e_tradeandroid.model.TradeStatus.PENDING_CONFIRM, "USER");
                }
                version++;
                isLoading.postValue(false);
                callback.onSuccess(null);
            }
            
            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
                callback.onFailure(error);
            }
        });
    }
    
    // ==================== State Checks ====================
    
    private boolean isDataReady() {
        return productLoaded.getValue() != null && productLoaded.getValue() &&
               tradeLoaded.getValue() != null && tradeLoaded.getValue();
    }
    
    public boolean canConfirm() {
        return stateMachine != null && stateMachine.isPendingConfirm();
    }
    
    public boolean canComplete() {
        return stateMachine != null && stateMachine.isConfirmed();
    }
    
    public boolean canConfirmReceive() {
        return stateMachine != null && stateMachine.isWaitingForOther();
    }
    
    public boolean canCancel() {
        return stateMachine != null && !stateMachine.isTerminated();
    }
    
    public boolean canUpdate() {
        return stateMachine != null && stateMachine.isConfirmed();
    }
    
    // ==================== Form Data ====================
    
    public void setBuyerPhone(String phone) {
        buyerPhone.postValue(phone);
        validateForm();
    }
    
    public void setSellerPhone(String phone) {
        sellerPhone.postValue(phone);
        validateForm();
    }
    
    public void setMeetingLocation(String location) {
        meetingLocation.postValue(location);
        validateForm();
    }
    
    public void setMeetingTime(String time) {
        meetingTime.postValue(time);
        validateForm();
    }
    
    private void validateForm() {
        String phone = sellerPhone.getValue() != null ? sellerPhone.getValue() : "";
        String location = meetingLocation.getValue() != null ? meetingLocation.getValue() : "";
        String time = meetingTime.getValue() != null ? meetingTime.getValue() : "";
        
        boolean valid = !location.isEmpty() && !time.isEmpty();
        if (stateMachine != null && stateMachine.isPendingConfirm()) {
            valid &= phone.length() == 11;
        }
        
        isSubmitEnabled.postValue(valid && isDataReady());
    }
    
    // ==================== Version Control ====================
    
    public int getVersion() {
        return version;
    }
    
    public void incrementVersion() {
        version++;
    }
}