#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author: Mine DOGAN <mine.dogan@agem.com.tr>


from base.model.ContentType import ContentType
from base.model.MessageCode import MessageCode
from base.model.MessageType import MessageType
from base.plugin.AbstractCommand import AbstractCommand

class TakeScreenshot(AbstractCommand):
    
    def __init__(self, task, context):
        super(TakeScreenshot, self).__init__()
        self.task = task
        self.context = context
        
        self.screenshot = self.get_screenshot()

        self.install_scrot = 'apt-get -y install scrot'
        self.take_screenshot = 'scrot '+self.screenshot
        
    def handle_task(self):
#        process = self.context.execute(self.install_scrot)
#        process.wait()
        
#        process = self.context.execute(self.take_screenshot)
#        process.wait()
        md5sum = self.scope.getExecutionManager().get_md5_file(self.screenshot)
        self.scope.getMessager().send_file(self.screenshot)

        data={'md5':md5sum}

        self.create_response(message='_message', data=data, content_type=ContentType.IMAGE_PNG.value)

    def get_screenshot(self):
        return '/tmp/oner.png'

    def create_response(self, success=True, message=None, data=None, content_type=None):
       if success:
           self.context.put('responseCode', MessageCode.TASK_PROCESSED.value)
       else:
           self.context.put('responseCode', MessageCode.TASK_ERROR.value)
       self.context.put('responseMessage', message)
       self.context.put('responseData', data)
       self.context.put('contentType', content_type)

def handle_task(task, context):
    
    screenshot = TakeScreenshot(task, context)
    screenshot.handle_task()

