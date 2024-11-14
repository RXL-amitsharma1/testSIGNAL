<div class="modal fade" id="attachmentFileModal">
    <div class="modal-dialog modal-md">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Add File</label>
            </button>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <label class="control-label lbl-elipsis" id="attachmentTypeName">File Name</label>
                    <input type="text" class="form-control" id="attachmentName" maxlength="4000">
                </div>

                <div class="form-group">
                    <div class="radio radio-primary radio-inline p-r-5">
                        <input  id="filetypeAttach" name="filetype" type="radio" checked="checked" value="file">
                        <label for="filetypeAttach">Attach File</label>
                        <span class="required-indicator">*</span>
                    </div>
                    <div class="radio radio-primary radio-inline">
                        <input name="filetype" id="filetypeLink" type="radio" value="link">
                        <label for="filetypeLink">Reference Link</label>
                        <span class="required-indicator">*</span>
                    </div>
                </div>

                <div class="form-group">
                    <div class="file-uploader" data-provides="fileupload">
                        <input type="file" name="safetyData.literatures[0].literatureArticle.filePath" data-mandatory="false" data-mandatoryset="0" class="file ">

                        <div class="input-group">
                            %{--                            <input type="file" id="hiddenAttachment">--}%
                            <input type="text" class="form-control "  placeholder="Attach a file" id="literatureFilePath" name="safetyData.fileInputBox[-1].fileName" value="" title=""
                                   oninput="javascript: if (this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);">

                            <input type="hidden" name="safetyData.literatures[0].literatureArticle.id" value="${signal.id}" id="safetyData.literatures[0].literatureArticle.id"><input type="hidden" name="safetyData.literatures[0].literatureArticle.fileName" value="" id="safetyData.literatures[0].literatureArticle.fileName">
                            <input type="hidden" id="validatedSignalid" value="${signal.id}"/>
                            <span class="input-group-btn ">
                                <button class="browse btn btn-primary btn-file-upload " type="button"><i class="glyphicon glyphicon-search" id="uploadAttachment"></i>
                                </button>
                            </span>


                        </div>
                    </div>

                    <div class="hide" id="fileLinkControl">
                        <input type="text" class="form-control" placeholder="Enter reference URL"/>
                    </div>

                </div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"  id="closeAttachModal">
                    Cancel
                </button>
                <button type="button" id="addFile" class="btn btn-primary" data-dismiss="modal">
                    Add
                </button>
            </div>
        </div>
    </div>
</div>