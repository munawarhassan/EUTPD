import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { SvgIcons } from '@devacfr/bootstrap';
import { MonitoringService } from '@devacfr/core';
import { NotifierService } from '@devacfr/layout';
import { Subscription } from 'rxjs';
import { MetricsMemoryComponent } from './metrics-memory.component';
import { MetricsNetworkComponent } from './metrics-network.component';
import { MetricsProcessorComponent } from './metrics-processor.component';
import { MetricsThreadDumpComponent } from './metrics-threaddump-component';
import { MetricsThreadsComponent } from './metrics-threads.component';

@Component({
    selector: 'app-metrics',
    templateUrl: './metrics.component.html',
})
export class MetricsComponent implements OnInit, OnDestroy {
    @ViewChild(MetricsMemoryComponent, { static: true })
    public memoryComponent!: MetricsMemoryComponent;

    @ViewChild(MetricsProcessorComponent, { static: true })
    public processorComponent!: MetricsProcessorComponent;

    @ViewChild(MetricsThreadsComponent, { static: true })
    public threadsComponent!: MetricsThreadsComponent;

    @ViewChild(MetricsThreadDumpComponent, { static: true })
    public threadDumpComponent!: MetricsThreadDumpComponent;

    @ViewChild(MetricsNetworkComponent)
    public networkComponent!: MetricsNetworkComponent;

    public servicesStats = {};
    public cachesStats = {};

    private metrics = {};
    private subscriptions = new Subscription();

    constructor(public svgIcons: SvgIcons) {}

    public ngOnInit() {
        this.refresh();
    }

    public ngOnDestroy(): void {
        this.subscriptions.unsubscribe();
    }

    public refresh() {
        /*
    this._monitoringService.getMetrics().subscribe(
      (data) => {
        this.metrics = data;
        this.buildOtherStatitic(this.metrics);
      },
      (err) => {
        this._notifierService.error(err);
        this.metrics = null;
      }
    );
    */
    }

    public handleSelectTab(tab) {
        if (tab.id === 'm_portlet_general') {
            this.memoryComponent.start();
            this.processorComponent.start();
        } else {
            this.memoryComponent.stop();
            this.processorComponent.stop();
        }
        if (tab.id === 'm_portlet_thread') {
            this.threadsComponent.start();
            this.threadDumpComponent.start();
        } else {
            this.threadsComponent.stop();
            this.threadDumpComponent.stop();
        }
        if (tab.id === 'm_portlet_network') {
            this.networkComponent.start();
        } else {
            this.networkComponent.stop();
        }
    }

    private buildOtherStatitic(metrics) {
        this.servicesStats = {};
        this.cachesStats = {};
        /*
    angular.forEach(metrics.timers, function (value, key) {
      if (key.indexOf('rsrc.api') !== -1) {
        const index = key.indexOf('rsrc.api');
        this.servicesStats[key.substr(index)] = value;
      } else if (key.indexOf('service') !== -1) {
        const index = key.indexOf('service');
        this.servicesStats[key.substr(index)] = value;
      } else if (key.indexOf('net.sf.ehcache.Cache') !== -1) {
        // remove gets or puts
        let index = key.lastIndexOf('.');
        const newKey = key.substr(0, index);

        // Keep the name of the
        // domain
        index = newKey.lastIndexOf('.');
        this.cachesStats[newKey] = {
          'name': newKey.substr(index + 1),
          'value': value
        };
      }
    });
    */
    }
}
