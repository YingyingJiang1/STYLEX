
    private static void addAttachmentAnnotationsToDocument(
            final PDDocument document, final List<EmailAttachment> attachments) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("addAttachmentAnnotationsToDocument() called: document.pages=[{}], attachments.size=[{}]",
                    document != null ? document.getNumberOfPages() : 0, 
                    attachments != null ? attachments.size() : 0);
        }

        final boolean noPages = document.getNumberOfPages() == 0;
        final boolean noAttachments = attachments == null || attachments.isEmpty();
        if (noPages || noAttachments) {
            return;
        }

        // 1. Locate all attachment markers in document
        final AttachmentMarkerPositionFinder finder = new AttachmentMarkerPositionFinder();
        finder.setSortByPosition(true);
        finder.getText(document);
        final List<MarkerPosition> markerPositions = finder.getPositions();

        // 2. Validate marker-attachment count match
        final boolean countMismatch = markerPositions.size() != attachments.size();
        if (countMismatch) {
            log.warn(
                    "Found {} attachment markers, but there are {} attachments. Annotation count may be incorrect.",
                    markerPositions.size(),
                    attachments.size());
        }

        // 3. Create invisible annotations for each valid marker
        final int annotationsToAdd = Math.min(markerPositions.size(), attachments.size());
        for (int i = 0; i < annotationsToAdd; i++) {
            final MarkerPosition position = markerPositions.get(i);
            final EmailAttachment attachment = attachments.get(i);

            final boolean hasEmbeddedFilename = attachment.getEmbeddedFilename() != null;
            if (hasEmbeddedFilename) {
                final PDPage page = document.getPage(position.getPageIndex());
                addAttachmentAnnotationToPage(
                        document, page, attachment, position.getX(), position.getY());
            }
        }
    }
