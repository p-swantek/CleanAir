var getNodes = function(heatmap) {
    window.nodes = {};
    window.nodes.min = 0;
    window.nodes.max = 15;


    $.get("api/nodes", function(nodes) {
        window.nodes.data = nodes;
        heatmap.setData(window.nodes);
    });
};

