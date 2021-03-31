package com.nai.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import com.nai.gulimall.common.to.SkuInfoTo;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.cart.feign.ProductFeignService;
import com.nai.gulimall.cart.interceptor.CartInterceptor;
import com.nai.gulimall.cart.service.CartService;
import com.nai.gulimall.cart.vo.Cart;
import com.nai.gulimall.cart.vo.CartItem;
import com.nai.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author TheNai
 * @date 2021-03-20 14:48
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;


    private static final String CART_PREFIX = "GULIMALL:CART:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOperations = getCartOperations();
        String result = (String) cartOperations.get(skuId.toString());
        if (StringUtils.isEmpty(result)) {
            CartItem cartItem = new CartItem();
            //购物车无此商品
            //2.添加新商品到购物车
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                //1.远程查询当前要添加的商品的信息
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoTo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoTo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setPrice(data.getPrice());
                cartItem.setSkuId(skuId);
            }, executor);
            //3.远程查询sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, executor);
            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();
            String json = JSON.toJSONString(cartItem);
            cartOperations.put(skuId.toString(), json);
            return cartItem;
        } else {
            //购物车有这个商品
            CartItem cartItem = JSON.parseObject(result, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOperations.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOperations = getCartOperations();
        String json = (String) cartOperations.get(skuId.toString());
        return JSON.parseObject(json, CartItem.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        //1.区分登录还是不登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            //登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //如果临时购物车的数据还没有合并
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (CollectionUtils.isNotEmpty(tempCartItems)) {
                //临时购物车有数据,需要合并
                for (CartItem item : tempCartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
                //清除临时购物车的数据
                clearCart(tempCartKey);
            } else {
                //3.获取登录后的购物车(包含合并来的临时购物车的数据,和登陆后的购物车的数据)
                List<CartItem> cartItems = getCartItems(cartKey);
                cart.setItems(cartItems);
            }
        } else {
            //没登陆
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    /**
     * 获取到我们要操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOperations() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //1.
        String cartKey;
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (CollectionUtils.isNotEmpty(values)) {
            return values.stream().map(obj -> {
                String str = (String) obj;
                return JSON.parseObject(str, CartItem.class);
            }).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> operations = getCartOperations();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String json = JSON.toJSONString(cartItem);
        operations.put(skuId.toString(), json);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> operations = getCartOperations();
        operations.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> operations = getCartOperations();
        operations.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            assert cartItems != null;
            return cartItems.stream().filter(CartItem::getCheck)
                    .peek(item -> {
                        R price = productFeignService.getPrice(item.getSkuId());
                        //更新为最新价格
                        String data = (String) price.get("data");
                        item.setPrice(new BigDecimal(data));
                    }).collect(Collectors.toList());
        }
    }
}
