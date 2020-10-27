package im.joker.event;

import im.joker.event.content.IContent;

/**
 * 包含一个<code>content</code>字段的基本事件。
 *
 * @author linyurong
 */
public interface ImEvent {


    /**
     * 事件的类型。
     *
     * @return type
     */
    String getType();

    /**
     * 事件的主要内容。
     *
     * @return main content
     */
    IContent getContent();
}
