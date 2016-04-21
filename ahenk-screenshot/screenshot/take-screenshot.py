#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author: Mine DOGAN <mine.dogan@agem.com.tr>


import datetime
from base.model.enum.ContentType import ContentType
from base.model.enum.MessageCode import MessageCode
from base.plugin.AbstractCommand import AbstractCommand


class TakeScreenshot(AbstractCommand):
    def __init__(self, task, context):
        super(TakeScreenshot, self).__init__()
        self.task = task
        self.context = context
        self.shot_path = self.get_shot_path()

        self.take_screenshot = '/bin/bash ./plugins/screenshot/scripts/screenshot.sh ' + self.shot_path

    def handle_task(self):

        process = self.context.execute(self.take_screenshot)
        process.wait()

        md5sum = self.scope.getExecutionManager().get_md5_file(str(self.shot_path))
        print('md5:' + md5sum)

        try:
            self.scope.getMessager().send_file(self.shot_path)
        except Exception as e:
            print('--->' + str(e))

        data = {'md5': md5sum}

        self.create_response(message='_message', data=data, content_type=ContentType.IMAGE_JPEG.value)

    def get_shot_path(self):
        return '/tmp/ahenk_screenshot_' + str(datetime.datetime.now().strftime("%d%m%Y%I%M")) + '.jpg'

    def create_response(self, success=True, message=None, data=None, content_type=None):
        if success:
            self.context.put('responseCode', MessageCode.TASK_PROCESSED.value)
        else:
            self.context.put('responseCode', MessageCode.TASK_ERROR.value)
        self.context.put('responseMessage', message)
        self.context.put('responseData', data)
        self.context.put('contentType', content_type)


def handle_task(task, context):
    print('TAKE SHOT')
    screenshot = TakeScreenshot(task, context)
    screenshot.handle_task()
