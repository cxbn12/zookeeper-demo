package org.donald.zookeeper.get;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.donald.constant.ConfigConstant;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName: GetChildrenAsyncUsage
 * @Description: ZooKeeper API 获取子节点列表，使用异步(ASync)接口。
 * 注意，如果重新注册监听器，必须在下一个事件操作之前，如果同时操作，或者前后，很有可能监听不到相关的事件。
 * 相同的问题，见
 * @see org.donald.zookeeper.exists.ExistsSyncUsage
 * @see  ExistsSyncUsage {@link #process(WatchedEvent)}
 * @see org.donald.zookeeper.exists.ExistsSyncUsage {@link #process(WatchedEvent)}
 * {@link org.donald.zookeeper.exists.ExistsSyncUsage}
 * {@link ExistsSyncUsage {@link #process(WatchedEvent)}}
 * {@link org.donald.zookeeper.exists.ExistsSyncUsage {@link #process(WatchedEvent)}}
 * 三种类和方法引用的示例。
 * @Author: Donaldhan
 * @Date: 2018-05-12 13:58
 */
@Slf4j
public class GetChildrenAsyncUsage implements Watcher{
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk = null;
    public static void main(String[] args) throws Exception{
        try {
            String path = "/zk-book";
            zk = new ZooKeeper(ConfigConstant.IP,ConfigConstant.SESSION_TIMEOUT,new GetChildrenAsyncUsage());
            connectedSemaphore.await();
            zk.create(path, "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            Thread.sleep( 6000);
            zk.create(path+"/c1", "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            //异步获取路径孩子节点
            zk.getChildren(path, true, new IChildren2Callback(), null);
            Thread.sleep( 6000);
            zk.create(path+"/c2", "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            Thread.sleep( 6000);
            //每次path节点的字节点改变将处罚NodeChildrenChanged事件
            zk.create(path+"/c3", "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } finally {
            if(null != zk){
                zk.close();
            }
        }
    }
    @Override
    public void process(WatchedEvent event) {
        if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
            if (Watcher.Event.EventType.None == event.getType() && null == event.getPath()) {
                connectedSemaphore.countDown();
            } else if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                try {
                  /**
                   *  @param path
                   *  @param watch 默认注册创建zk客户端事件的监听器，具体见
                   *  @return an unordered array of children of the node with the given path
                   *  @throws InterruptedException If the server transaction is interrupted.
                   *  @throws KeeperException If the server signals an error with a non-zero error code.
                   *  public List<String> getChildren(String path, boolean watch)
                   *  throws KeeperException, InterruptedException {
                   *  return getChildren(path, watch ? watchManager.defaultWatcher : null);
                    }*/
                    log.info("ReGet Child:{}",zk.getChildren(event.getPath(),true));
                } catch (Exception e) {}
            }
        }
    }
}

/**
 * 异步回调接口
 */
@Slf4j
class IChildren2Callback implements AsyncCallback.Children2Callback{
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        log.info("Get Children znode result: [response code:{}, param path:{}, ctx:{}, " +
                "children list: {}, stat:{} ",new Object[]{rc,path,ctx,children,stat} );
    }
}
