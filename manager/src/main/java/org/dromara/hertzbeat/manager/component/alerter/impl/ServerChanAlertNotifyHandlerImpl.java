package org.dromara.hertzbeat.manager.component.alerter.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hertzbeat.common.entity.alerter.Alert;
import org.dromara.hertzbeat.common.entity.manager.NoticeReceiver;
import org.dromara.hertzbeat.common.entity.manager.NoticeTemplate;
import org.dromara.hertzbeat.manager.support.exception.AlertNoticeException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

/**
 * Server酱发送
 *
 * @author zqr10159
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServerChanAlertNotifyHandlerImpl extends AbstractAlertNotifyHandlerImpl {
    /**
     * 发送报警通知
     *
     * @param receiver Notification configuration information   通知配置信息
     * @param alert    Alarm information                        告警信息
     * @throws AlertNoticeException when send receiver error
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, Alert alert) throws AlertNoticeException {
        try {
            ServerChanAlertNotifyHandlerImpl.ServerChanWebHookDto serverChanWebHookDto = new ServerChanAlertNotifyHandlerImpl.ServerChanWebHookDto();
            serverChanWebHookDto.setTitle(bundle.getString("alerter.notify.title"));
            serverChanWebHookDto.setDesp(renderContent(noticeTemplate, alert));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServerChanAlertNotifyHandlerImpl.ServerChanWebHookDto> httpEntity = new HttpEntity<>(serverChanWebHookDto, headers);
            String webHookUrl = String.format(alerterProperties.getServerChanNotifyUrl(), receiver.getServerChanToken());
            ResponseEntity<CommonRobotNotifyResp> responseEntity = restTemplate.postForEntity(webHookUrl,
                    httpEntity, CommonRobotNotifyResp.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                log.debug("Send ServerChan webHook: {} Success", webHookUrl);
            } else {
                log.warn("Send ServerChan webHook: {} Failed: {}", webHookUrl, responseEntity.getBody());
                throw new AlertNoticeException("Http StatusCode " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            throw new AlertNoticeException("[ServerChan Notify Error] " + e.getMessage());
        }
    }

    /**
     * 通知类型
     *
     * @return 通知类型
     */
    @Override
    public byte type() {
        return 12;
    }

    @Data
    private static class ServerChanWebHookDto {
        private static final String MARKDOWN = "markdown";
        /**
         * 标题
         */
        private String title;
        /**
         * markdown消息内容
         */
        private String desp;

    }


}
