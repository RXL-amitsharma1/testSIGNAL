$(function () {
    function saveWidgets() {
        var serializedData = _.map($('.grid-stack > .grid-stack-item:visible'), function (el) {
            el = $(el);
            var node = el.data('_gridstack_node');
            return {
                id: node.id,
                x: node.x,
                y: node.y,
                width: node.width,
                height: node.height
            };
        }, this);
        $.ajax({
            type: "POST",
            url: CONFIGURATION.updateWidgetsUrl,
            dataType: "json",
            data: {items: JSON.stringify(serializedData, null, '    ')},
            error: function (err) {
                console.log(err);
            }
        });
    }

    $(".remove-widget").click(function() {
        var widget = $(this).closest(".grid-stack-item");
        var url = $(this).data("url");
        var params = $(this).data("params");
        if (CONFIGURATION[url]) {
            $.ajax({
                type: "POST",
                url: CONFIGURATION[url],
                dataType: "json",
                data: params,
                error: function (err) {
                    console.log(err);
                },
                success: function (data) {
                    var grid = $('.grid-stack').data('gridstack');
                    grid.removeWidget(widget);
                }
            });
        }
    });
    $( ".rx-widget-menu-content").menu({
        select: function( event, ui ) {
            var url = ui.item.data("url");
            var params = ui.item.data("params");
            if (CONFIGURATION[url]) {
                window.location.href = CONFIGURATION[url] + "?" + $.param(params);
            }
        }
    });

    var gridstackOptions = {
        width: 12,
        resizable: {autoHide: true, handles: 'all'},
        verticalMargin: 10
    };
    $('.grid-stack').gridstack(gridstackOptions);
    $('.grid-stack').on('change', function(event, items) {
        saveWidgets();
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var chartContainer = $(item.el).find('.chart-container').first();
            if (chartContainer.length) {
                if (typeof chartContainer.highcharts() != "undefined" && chartContainer.highcharts() != null) {
                    chartContainer.highcharts().setSize(chartContainer.innerWidth()-25,chartContainer.innerHeight()-20);
                }
            }
        }
    });
    $(window).resize(function() {
        $('.chart-container').each(function(){
            var chart=$(this);
            chart.highcharts().setSize(chart.innerWidth()-25,chart.innerHeight()-20);
        });
        $('.widget-calendar').each(function(){
            var calendar=$(this);
            calendar.fullCalendar('option', 'contentHeight', calendar.parent().height()-150);
        });
    });
    $('.chart-container').each(function(index, element) {
        var container = $(this);
        var options = JSON.parse(container.attr("data"),function(key, value) {
            if (key === "formatter") {
                value = eval("(" + value + ")")
            }
            return value;
        });
        container.highcharts(options);
    });

    function checkIfSessionTimeOutThenReload(event, json) {
        if (json && json[SESSION_TIME_OUT]) {
            event ? event.stopPropagation() : "";
            alert($.i18n._('sessionTimeOut'));
            window.location.reload();
            return false
        }
    }
});