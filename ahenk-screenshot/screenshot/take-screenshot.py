#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author: Mine DOGAN <mine.dogan@agem.com.tr>
# Author: Emre Akkaya <emre.akkaya@agem.com.tr>


from base.plugin.abstract_plugin import AbstractPlugin
import json
import traceback


class TakeScreenshot(AbstractPlugin):
    def __init__(self, data, context):
        super(TakeScreenshot, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

        self.temp_file_name = str(self.generate_uuid())
        self.shot_path = '{0}{1}'.format(str(self.Ahenk.received_dir_path()), self.temp_file_name)
        self.take_screenshot = '/bin/bash {0}screenshot/scripts/screenshot.sh {1}'.format(self.Ahenk.plugins_path(),
                                                                                          self.shot_path)

    def handle_task(self):
        try:
            user_name = None

            if self.has_attr_json(self.data, self.Ahenk.dn()) and self.data[self.Ahenk.dn()] is not None:
                user_name = self.data[self.Ahenk.dn()]

            if not user_name:
                self.logger.debug('[SCREENSHOT] Taking screenshot with default display.')
                self.execute(self.take_screenshot)

            else:
                user_display = self.Sessions.display(user_name)
                if not user_display:
                    user_display = '0'

                ##permission
                self.logger.error(
                    '[SCREENSHOT] Asking for screenshot to user {0} on {1} display'.format(user_name, user_display))

                user_answer = self.ask_permission(user_display, user_name,
                                                  "Ekran görüntüsünün alınmasına izin veriyor musunuz?",
                                                  "Ekran Görüntüsü")

                if user_answer is None:
                    self.logger.error('[SCREENSHOT] User answer could not keep.')
                    self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                                 message='Ekran görüntüsü alırken hata oluştu: Kullanıcı iznine erişilemedi.')
                    return

                elif user_answer is True:
                    self.logger.debug('[SCREENSHOT] User accepted for screenshot')
                    self.logger.debug('[SCREENSHOT] Taking screenshot with specified display: {0}'.format(user_display))
                    self.execute(self.take_screenshot + ' ' + user_display.replace(':', ''))
                    self.logger.debug('[SCREENSHOT] Screenshot command executed.')
                else:
                    self.logger.warning('[SCREENSHOT] User decline to screenshot.')
                    self.context.create_response(code=self.message_code.TASK_WARNING.value,
                                                 message='Eklenti başatıyla çalıştı fakat; kullanıcı ekran görüntüsü alınmasına izin vermedi.')
                    return
                ##permission###

            if self.is_exist(self.shot_path):
                self.logger.debug('[SCREENSHOT] Screenshot file found.')

                data = {}
                md5sum = self.get_md5_file(str(self.shot_path))
                self.logger.debug('[SCREENSHOT] {0} renaming to {1}'.format(self.temp_file_name, md5sum))
                self.rename_file(self.shot_path, self.Ahenk.received_dir_path() + md5sum)
                self.logger.debug('[SCREENSHOT] Renamed.')
                data['md5'] = md5sum
                self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                             message='Ekran görüntüsü başarıyla alındı.',
                                             data=json.dumps(data),
                                             content_type=self.get_content_type().IMAGE_JPEG.value)
                self.logger.debug('[SCREENSHOT] SCREENSHOT task is handled successfully')
            else:
                raise Exception('Image not found this path: {}'.format(self.shot_path))

        except Exception as e:
            self.logger.error(
                '[SCREENSHOT] A problem occured while handling SCREENSHOT task: {0}'.format(traceback.format_exc()))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Ekran görüntüsü alırken hata oluştu: {0}'.format(str(e)))


def handle_task(task, context):
    screenshot = TakeScreenshot(task, context)
    screenshot.handle_task()
