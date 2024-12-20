$('#zoomBtn').click(function() {
    $('.zoom-btn-sm').toggleClass('scale-out');
    if (!$('.zoom-card').hasClass('scale-out')) {
        $('.zoom-card').toggleClass('scale-out');
    }
});

$('.zoom-btn-sm').click(function() {
    var btn = $(this);
    var card = $('.zoom-card');

    if ($('.zoom-card').hasClass('scale-out')) {
        $('.zoom-card').toggleClass('scale-out');
    }
    if (btn.hasClass('zoom-btn-person')) {
        card.css('background-color', '#d32f2f');
    } else if (btn.hasClass('zoom-btn-doc')) {
        card.css('background-color', '#fbc02d');
    } else if (btn.hasClass('zoom-btn-tangram')) {
        card.css('background-color', '#388e3c');
    } else if (btn.hasClass('zoom-btn-report')) {
        card.css('background-color', '#1976d2');
    } else {
        card.css('background-color', '#7b1fa2');
    }
});
